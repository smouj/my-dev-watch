#!/usr/bin/env node
/**
 * Zero-dependency static server for the studio.
 *
 * It also exposes two tiny endpoints that a browser cannot provide on its own:
 * the LAN address (so a phone can reach the PC) and a QR code for that address.
 * Project files are written as plain JSON on disk, which is the whole "sync".
 */
import { createServer } from "node:http";
import { readFile, writeFile, mkdir, readdir } from "node:fs/promises";
import { existsSync } from "node:fs";
import { extname, join, resolve, dirname } from "node:path";
import { fileURLToPath } from "node:url";
import { networkInterfaces } from "node:os";

const ROOT = dirname(fileURLToPath(import.meta.url));
const PROJECTS = join(ROOT, "projects");
const PORT = Number(process.env.PORT || 5173);
const HOST = process.env.HOST || "0.0.0.0";

const MIME = {
    ".html": "text/html; charset=utf-8",
    ".css": "text/css; charset=utf-8",
    ".js": "text/javascript; charset=utf-8",
    ".json": "application/json; charset=utf-8",
    ".webmanifest": "application/manifest+json; charset=utf-8",
    ".png": "image/png",
    ".svg": "image/svg+xml",
    ".ico": "image/x-icon",
    ".txt": "text/plain; charset=utf-8"
};

function lanAddresses() {
    const result = [];
    const interfaces = networkInterfaces();
    for (const name of Object.keys(interfaces)) {
        for (const entry of interfaces[name] || []) {
            if (entry.family === "IPv4" && !entry.internal) result.push(entry.address);
        }
    }
    return result;
}

async function readBody(request, limit = 2 * 1024 * 1024) {
    const chunks = [];
    let size = 0;
    for await (const chunk of request) {
        size += chunk.length;
        if (size > limit) throw new Error("Body too large");
        chunks.push(chunk);
    }
    return Buffer.concat(chunks).toString("utf8");
}

function safeProjectName(name) {
    return String(name || "project")
        .toLowerCase()
        .replace(/[^a-z0-9-_]+/g, "-")
        .replace(/^-+|-+$/g, "")
        .slice(0, 60) || "project";
}

async function handleApi(request, response, url) {
    if (url.pathname === "/api/info") {
        const urls = lanAddresses().map((address) => "http://" + address + ":" + PORT);
        response.writeHead(200, { "content-type": "application/json; charset=utf-8", "cache-control": "no-store" });
        response.end(JSON.stringify({ port: PORT, urls, local: "http://127.0.0.1:" + PORT }));
        return true;
    }

    if (url.pathname === "/api/projects" && request.method === "GET") {
        await mkdir(PROJECTS, { recursive: true });
        const files = (await readdir(PROJECTS)).filter((file) => file.endsWith(".json"));
        const projects = [];
        for (const file of files) {
            try {
                const raw = await readFile(join(PROJECTS, file), "utf8");
                const parsed = JSON.parse(raw);
                projects.push({ id: file.replace(/\.json$/, ""), name: parsed.name || file, updated: parsed.updated || null });
            } catch {
                // A corrupt project is skipped, never silently overwritten.
            }
        }
        response.writeHead(200, { "content-type": "application/json; charset=utf-8" });
        response.end(JSON.stringify({ projects }));
        return true;
    }

    if (url.pathname === "/api/projects" && request.method === "POST") {
        const body = JSON.parse(await readBody(request));
        const id = safeProjectName(body.name);
        if (!body.project || typeof body.project !== "object") {
            response.writeHead(400, { "content-type": "application/json" });
            response.end(JSON.stringify({ error: "Missing project" }));
            return true;
        }
        await mkdir(PROJECTS, { recursive: true });
        const payload = { ...body.project, updated: new Date().toISOString() };
        await writeFile(join(PROJECTS, id + ".json"), JSON.stringify(payload, null, 2), "utf8");
        response.writeHead(200, { "content-type": "application/json" });
        response.end(JSON.stringify({ saved: id }));
        return true;
    }

    if (url.pathname.startsWith("/api/projects/") && request.method === "GET") {
        const id = safeProjectName(url.pathname.slice("/api/projects/".length));
        const file = join(PROJECTS, id + ".json");
        if (!existsSync(file)) {
            response.writeHead(404, { "content-type": "application/json" });
            response.end(JSON.stringify({ error: "Not found" }));
            return true;
        }
        response.writeHead(200, { "content-type": "application/json; charset=utf-8" });
        response.end(await readFile(file, "utf8"));
        return true;
    }

    return false;
}

const server = createServer(async (request, response) => {
    try {
        const url = new URL(request.url || "/", "http://localhost:" + PORT);
        if (url.pathname.startsWith("/api/")) {
            const handled = await handleApi(request, response, url);
            if (!handled) {
                response.writeHead(404, { "content-type": "application/json" });
                response.end(JSON.stringify({ error: "Unknown endpoint" }));
            }
            return;
        }

        const relative = url.pathname === "/" ? "index.html" : decodeURIComponent(url.pathname).replace(/^\/+/, "");
        const target = resolve(join(ROOT, relative));
        if (!target.startsWith(resolve(ROOT)) || !existsSync(target) || !extname(target)) {
            response.writeHead(404, { "content-type": "text/plain; charset=utf-8" });
            response.end("Not found");
            return;
        }
        const file = await readFile(target);
        response.writeHead(200, {
            "content-type": MIME[extname(target)] || "application/octet-stream",
            "cache-control": target.endsWith("sw.js") ? "no-store" : "no-cache"
        });
        response.end(file);
    } catch (error) {
        response.writeHead(500, { "content-type": "application/json" });
        response.end(JSON.stringify({ error: error.message }));
    }
});

server.listen(PORT, HOST, () => {
    const urls = lanAddresses().map((address) => "http://" + address + ":" + PORT);
    console.log("My Dev Watch studio");
    console.log("  local:  http://127.0.0.1:" + PORT);
    urls.forEach((url) => console.log("  phone:  " + url));
    console.log("Open the Install tab to show the QR code.");
});
