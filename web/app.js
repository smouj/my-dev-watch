/**
 * My Dev Watch studio.
 *
 * Plain modules, no build step, no runtime dependency: the file a phone installs is
 * the file a human can read. Project state is a small JSON document that can be
 * saved on the PC (via the local server) or exported as a .mdwface file.
 */

const $ = (selector) => document.querySelector(selector);
const $$ = (selector) => Array.from(document.querySelectorAll(selector));

const STORAGE_KEY = "my-dev-watch.project";
const THEME_KEY = "my-dev-watch.theme";
const CANVAS = 480;

const uid = () => Math.random().toString(36).slice(2, 9);

const defaultProject = () => ({
    format: "my-dev-watch/face",
    version: 1,
    name: "My first face",
    width: 240,
    height: 240,
    shape: "circle",
    background: "#0c1014",
    layers: [
        { id: uid(), type: "time", text: "08:24", x: 120, y: 96, size: 58, weight: 600, color: "#e9edf2" },
        { id: uid(), type: "date", text: "", x: 120, y: 138, size: 18, weight: 400, color: "#9aa5b1" },
        { id: uid(), type: "steps", text: "", x: 120, y: 170, size: 20, weight: 400, color: "#2fb6c4" },
        { id: uid(), type: "battery", text: "", x: 120, y: 198, size: 16, weight: 400, color: "#9aa5b1" }
    ]
});

const state = {
    project: loadLocal() || defaultProject(),
    selectedId: null,
    sim: { time: "08:24", steps: 8421, battery: 86, heart: 71 },
    dragging: null
};

/* ---------- persistence ---------- */

function loadLocal() {
    try {
        const raw = localStorage.getItem(STORAGE_KEY);
        if (!raw) return null;
        const parsed = JSON.parse(raw);
        return parsed && Array.isArray(parsed.layers) ? parsed : null;
    } catch {
        return null;
    }
}

function persistLocal() {
    try {
        localStorage.setItem(STORAGE_KEY, JSON.stringify(state.project));
        $("#storage-state").textContent = "Saved in this browser";
    } catch {
        $("#storage-state").textContent = "Local storage unavailable";
    }
}

function selected() {
    return state.project.layers.find((layer) => layer.id === state.selectedId) || null;
}

/* ---------- canvas ---------- */

const canvas = $("#face");
const ctx = canvas.getContext("2d");

function scaleFactor() {
    return CANVAS / state.project.width;
}

function layerText(layer) {
    switch (layer.type) {
        case "time": return state.sim.time || "08:24";
        case "date": return new Date().toLocaleDateString(undefined, { day: "2-digit", month: "short" });
        case "steps": return Number(state.sim.steps).toLocaleString() + " steps";
        case "battery": return state.sim.battery + " %";
        default: return layer.text || "Text";
    }
}

function fontFor(layer, scale) {
    return String(layer.weight || 400) + " " + Math.round(layer.size * scale) + "px " +
        "-apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, Inter, system-ui, sans-serif";
}

function draw() {
    const { width, height, shape, background } = state.project;
    const scale = scaleFactor();
    ctx.clearRect(0, 0, CANVAS, CANVAS);

    ctx.save();
    pathFor(shape, CANVAS, CANVAS);
    ctx.fillStyle = background;
    ctx.fill();
    ctx.restore();

    ctx.textAlign = "center";
    ctx.textBaseline = "middle";

    state.project.layers.forEach((layer) => {
        ctx.save();
        ctx.fillStyle = layer.color || "#e9edf2";
        ctx.font = fontFor(layer, scale);
        const x = layer.x * scale;
        const y = layer.y * scale;

        if (layer.type === "shape") {
            ctx.fillRect(x - layer.size * scale, y - layer.size * scale * 0.25, layer.size * 2 * scale, layer.size * scale * 0.5);
        } else {
            ctx.fillText(layerText(layer), x, y);
        }

        if (layer.id === state.selectedId) {
            const metrics = ctx.measureText(layer.type === "shape" ? "" : layerText(layer));
            const w = Math.max(metrics.width, layer.size * scale) + 18;
            const h = layer.size * scale + 18;
            ctx.strokeStyle = "#2fb6c4";
            ctx.lineWidth = 1.5;
            ctx.setLineDash([5, 4]);
            ctx.strokeRect(x - w / 2, y - h / 2, w, h);
        }
        ctx.restore();
    });

    drawBezel(shape);
}

function pathFor(shape, w, h) {
    ctx.beginPath();
    if (shape === "circle") {
        ctx.arc(w / 2, h / 2, w / 2 - 1, 0, Math.PI * 2);
    } else if (shape === "rounded") {
        const r = w * 0.17;
        ctx.roundRect(1, 1, w - 2, h - 2, r);
    } else {
        ctx.rect(1, 1, w - 2, h - 2);
    }
}

function drawBezel(shape) {
    ctx.save();
    pathFor(shape, CANVAS, CANVAS);
    ctx.strokeStyle = "rgba(47,182,196,.55)";
    ctx.lineWidth = 3;
    ctx.stroke();
    ctx.restore();
}

/* ---------- layer list ---------- */

function renderLayers() {
    const list = $("#layer-list");
    list.innerHTML = "";
    state.project.layers.forEach((layer) => {
        const item = document.createElement("li");
        item.dataset.id = layer.id;
        if (layer.id === state.selectedId) item.classList.add("is-selected");
        item.innerHTML = '<span class="dot"></span><span class="grow">' +
            (layer.type === "text" ? (layer.text || "Text") : layer.type) + '</span>' +
            '<span class="kind">' + layer.type + '</span>';
        item.addEventListener("click", () => select(layer.id));
        list.appendChild(item);
    });
}

function select(id) {
    state.selectedId = id;
    renderLayers();
    renderProps();
    draw();
}

/* ---------- properties ---------- */

function renderProps() {
    const layer = selected();
    $("#props-empty").hidden = Boolean(layer);
    $("#props-form").hidden = !layer;
    if (!layer) return;
    $("#prop-text").value = layer.text || "";
    $("#prop-size").value = layer.size;
    $("#prop-x").value = layer.x;
    $("#prop-y").value = layer.y;
    $("#prop-weight").value = String(layer.weight || 400);
    $("#prop-color").value = layer.color || "#e9edf2";
}

function updateSelected(patch) {
    const layer = selected();
    if (!layer) return;
    Object.assign(layer, patch);
    persistLocal();
    draw();
    renderLayers();
}

function bindProps() {
    $("#prop-text").addEventListener("input", (event) => updateSelected({ text: event.target.value, type: "text" }));
    $("#prop-size").addEventListener("input", (event) => updateSelected({ size: Number(event.target.value) }));
    $("#prop-x").addEventListener("input", (event) => updateSelected({ x: Number(event.target.value) }));
    $("#prop-y").addEventListener("input", (event) => updateSelected({ y: Number(event.target.value) }));
    $("#prop-weight").addEventListener("change", (event) => updateSelected({ weight: Number(event.target.value) }));
    $("#prop-color").addEventListener("input", (event) => updateSelected({ color: event.target.value }));

    $("#btn-duplicate").addEventListener("click", () => {
        const layer = selected();
        if (!layer) return;
        const copy = { ...layer, id: uid(), x: layer.x + 10, y: layer.y + 10 };
        state.project.layers.push(copy);
        select(copy.id);
    });

    $("#btn-delete").addEventListener("click", () => {
        state.project.layers = state.project.layers.filter((layer) => layer.id !== state.selectedId);
        state.selectedId = null;
        persistLocal();
        renderLayers();
        renderProps();
        draw();
    });

    ["width", "height"].forEach((key) => {
        $("#prop-" + key).value = state.project[key];
        $("#prop-" + key).addEventListener("change", (event) => {
            state.project[key] = Number(event.target.value);
            draw();
            persistLocal();
        });
    });
    $("#prop-shape").value = state.project.shape;
    $("#prop-shape").addEventListener("change", (event) => {
        state.project.shape = event.target.value;
        draw();
        persistLocal();
    });
}

/* ---------- dragging ---------- */

function canvasPoint(event) {
    const rect = canvas.getBoundingClientRect();
    return {
        x: (event.clientX - rect.left) * (CANVAS / rect.width),
        y: (event.clientY - rect.top) * (CANVAS / rect.height)
    };
}

function hitTest(point) {
    const scale = scaleFactor();
    for (let index = state.project.layers.length - 1; index >= 0; index -= 1) {
        const layer = state.project.layers[index];
        ctx.font = fontFor(layer, scale);
        const metrics = ctx.measureText(layer.type === "shape" ? "MMMM" : layerText(layer));
        const w = Math.max(metrics.width, layer.size * scale) / 2 + 8;
        const h = (layer.size * scale) / 2 + 8;
        if (Math.abs(point.x - layer.x * scale) <= w && Math.abs(point.y - layer.y * scale) <= h) {
            return layer;
        }
    }
    return null;
}

canvas.addEventListener("pointerdown", (event) => {
    const point = canvasPoint(event);
    const layer = hitTest(point);
    if (!layer) {
        state.selectedId = null;
        renderLayers();
        renderProps();
        draw();
        return;
    }
    select(layer.id);
    state.dragging = {
        layer,
        offsetX: point.x / scaleFactor() - layer.x,
        offsetY: point.y / scaleFactor() - layer.y
    };
    canvas.setPointerCapture(event.pointerId);
});

canvas.addEventListener("pointermove", (event) => {
    if (!state.dragging) return;
    const point = canvasPoint(event);
    const scale = scaleFactor();
    const layer = state.dragging.layer;
    layer.x = Math.round(point.x / scale - state.dragging.offsetX);
    layer.y = Math.round(point.y / scale - state.dragging.offsetY);
    $("#prop-x").value = layer.x;
    $("#prop-y").value = layer.y;
    draw();
});

canvas.addEventListener("pointerup", () => {
    if (!state.dragging) return;
    state.dragging = null;
    persistLocal();
});

/* ---------- project actions ---------- */

function setProject(project) {
    state.project = project;
    state.selectedId = null;
    $("#project-name").value = project.name || "Untitled";
    $("#prop-width").value = project.width;
    $("#prop-height").value = project.height;
    $("#prop-shape").value = project.shape;
    renderLayers();
    renderProps();
    draw();
}

async function refreshProjectList() {
    const select = $("#open-project");
    try {
        const response = await fetch("/api/projects");
        if (!response.ok) throw new Error("HTTP " + response.status);
        const payload = await response.json();
        const current = select.value;
        select.innerHTML = '<option value="">Open…</option>';
        (payload.projects || []).forEach((project) => {
            const option = document.createElement("option");
            option.value = project.id;
            option.textContent = project.name || project.id;
            select.appendChild(option);
        });
        select.value = current;
    } catch {
        select.innerHTML = '<option value="">Local only</option>';
    }
}

async function loadProject(id) {
    if (!id) return;
    try {
        const response = await fetch("/api/projects/" + encodeURIComponent(id));
        if (!response.ok) throw new Error("HTTP " + response.status);
        const project = await response.json();
        setProject(project);
        $("#project-name").value = project.name || id;
        persistLocal();
        $("#save-status").textContent = "Opened " + id + ".json from the PC.";
    } catch (error) {
        $("#save-status").textContent = "Could not open " + id + ": " + error.message;
    }
}

function bindProjectActions() {
    $("#open-project").addEventListener("change", (event) => loadProject(event.target.value));

    $("#project-name").addEventListener("input", (event) => {
        state.project.name = event.target.value;
        persistLocal();
    });

    $("#btn-save").addEventListener("click", async () => {
        const status = $("#save-status");
        persistLocal();
        try {
            const response = await fetch("/api/projects", {
                method: "POST",
                headers: { "content-type": "application/json" },
                body: JSON.stringify({ name: state.project.name, project: state.project })
            });
            if (!response.ok) throw new Error("HTTP " + response.status);
            const payload = await response.json();
            status.textContent = "Saved on the PC as " + payload.saved + ".json";
            refreshProjectList();
        } catch (error) {
            status.textContent = "Saved in this browser only (" + error.message + "). Export to keep a copy.";
        }
    });

    $("#btn-export").addEventListener("click", () => {
        const blob = new Blob([JSON.stringify(state.project, null, 2)], { type: "application/json" });
        const link = document.createElement("a");
        link.href = URL.createObjectURL(blob);
        link.download = (state.project.name || "face").replace(/[^a-z0-9-_]+/gi, "-").toLowerCase() + ".mdwface";
        link.click();
        URL.revokeObjectURL(link.href);
    });

    $("#file-import").addEventListener("change", async (event) => {
        const file = event.target.files && event.target.files[0];
        if (!file) return;
        try {
            const parsed = JSON.parse(await file.text());
            if (!parsed || !Array.isArray(parsed.layers)) throw new Error("Not a watch-face project");
            setProject(parsed);
            persistLocal();
        } catch (error) {
            $("#save-status").textContent = "Import failed: " + error.message;
        }
        event.target.value = "";
    });

    $$("[data-add]").forEach((button) => {
        button.addEventListener("click", () => {
            const type = button.dataset.add;
            const layer = {
                id: uid(),
                type,
                text: type === "text" ? "New text" : "",
                x: Math.round(state.project.width / 2),
                y: Math.round(state.project.height / 2),
                size: type === "time" ? 48 : 20,
                weight: 400,
                color: "#e9edf2"
            };
            state.project.layers.push(layer);
            select(layer.id);
            persistLocal();
        });
    });
}

/* ---------- simulated values ---------- */

function bindSimulation() {
    $("#sim-time").addEventListener("input", (event) => { state.sim.time = event.target.value; draw(); });
    $("#sim-steps").addEventListener("input", (event) => { state.sim.steps = Number(event.target.value); draw(); });
    $("#sim-battery").addEventListener("input", (event) => { state.sim.battery = Number(event.target.value); draw(); });
    $("#sim-heart").addEventListener("input", (event) => { state.sim.heart = Number(event.target.value); });
}

/* ---------- view switching ---------- */

function bindTabs() {
    $$(".tab").forEach((tab) => {
        tab.addEventListener("click", () => {
            $$(".tab").forEach((item) => item.classList.toggle("is-active", item === tab));
            $$(".view").forEach((view) => view.classList.toggle("is-active", view.id === "view-" + tab.dataset.view));
            if (tab.dataset.view === "install") refreshInstall();
        });
    });
}

/* ---------- install / QR ---------- */

async function refreshInstall() {
    const box = $("#qr-box");
    const urlNode = $("#install-url");
    try {
        const response = await fetch("/api/info");
        if (!response.ok) throw new Error("HTTP " + response.status);
        const info = await response.json();
        const url = (info.urls && info.urls[0]) || info.local;
        urlNode.textContent = url;
        renderQr(url);
        $("#install-note").textContent = info.urls && info.urls.length
            ? "Keep this PC awake while the phone is connected."
            : "No LAN address detected. Use a hotspot or check the firewall.";
    } catch (error) {
        const fallback = window.location.href.split("#")[0];
        urlNode.textContent = fallback;
        renderQr(fallback);
        $("#install-note").textContent = "Hosted page detected: this QR opens the public studio. Run the local server to get a LAN address instead.";
    }
}

function renderQr(url) {
    const box = $("#qr-box");
    try {
        const qr = qrcode(0, "M");
        qr.addData(url);
        qr.make();
        box.innerHTML = qr.createSvgTag({ cellSize: 6, margin: 4 });
    } catch {
        box.textContent = url;
    }
}

async function copyLink() {
    const text = $("#install-url").textContent;
    try {
        await navigator.clipboard.writeText(text);
        $("#install-note").textContent = "Link copied.";
    } catch {
        $("#install-note").textContent = text;
    }
}

/* ---------- Web Bluetooth ---------- */

const log = (line) => {
    const node = $("#ble-log");
    node.textContent += line + "\n";
    node.scrollTop = node.scrollHeight;
};

async function connectWatch() {
    const status = $("#ble-status");
    const batteryNode = $("#ble-battery");
    if (!navigator.bluetooth) {
        status.textContent = "Web Bluetooth unavailable in this browser";
        log("Use Chrome or Edge on the PC, or Chrome on Android. iOS Safari cannot do this.");
        return;
    }
    try {
        status.textContent = "Choosing a device…";
        const device = await navigator.bluetooth.requestDevice({
            acceptAllDevices: true,
            optionalServices: ["000055ff-0000-1000-8000-00805f9b34fb"]
        });
        $("#ble-name").textContent = device.name || device.id;
        log("Selected " + (device.name || "unnamed device"));

        const server = await device.gatt.connect();
        status.textContent = "Connected";
        log("GATT connected");

        const services = await server.getPrimaryServices();
        const list = $("#ble-services");
        list.innerHTML = "";
        services.forEach((service) => {
            const item = document.createElement("li");
            item.textContent = service.uuid;
            list.appendChild(item);
            log("Service " + service.uuid);
        });

        const primary = services.find((service) => service.uuid.startsWith("000055ff"));
        if (!primary) {
            log("55FF not present: this may not be a GloryFit-family watch.");
            return;
        }
        const writeCharacteristic = await primary.getCharacteristic("000033f1-0000-1000-8000-00805f9b34fb");
        const notifyCharacteristic = await primary.getCharacteristic("000033f2-0000-1000-8000-00805f9b34fb");

        await notifyCharacteristic.startNotifications();
        notifyCharacteristic.addEventListener("characteristicvaluechanged", (event) => {
            const view = event.target.value;
            const bytes = Array.from(new Uint8Array(view.buffer)).map((value) =>
                value.toString(16).padStart(2, "0").toUpperCase());
            log("RX " + bytes.join(" "));
            if (view.byteLength >= 2 && view.getUint8(0) === 0xA2) {
                const percent = view.getUint8(1);
                if (percent <= 100) {
                    batteryNode.textContent = percent + " %";
                    log("Battery " + percent + " %");
                } else {
                    log("Battery byte out of range, ignored");
                }
            }
        });

        log("TX A2");
        await writeCharacteristic.writeValueWithoutResponse(new Uint8Array([0xA2]));
        status.textContent = "Connected · battery request sent";
    } catch (error) {
        status.textContent = "Not connected";
        log("Error: " + error.message);
    }
}

/* ---------- theme ---------- */

function bindTheme() {
    const saved = localStorage.getItem(THEME_KEY);
    if (saved) document.documentElement.dataset.theme = saved;
    $("#theme-toggle").addEventListener("click", () => {
        const next = document.documentElement.dataset.theme === "light" ? "dark" : "light";
        document.documentElement.dataset.theme = next;
        localStorage.setItem(THEME_KEY, next);
    });
}

/* ---------- boot ---------- */

function boot() {
    bindTabs();
    bindTheme();
    bindProps();
    bindProjectActions();
    bindSimulation();
    $("#btn-copy").addEventListener("click", copyLink);
    $("#btn-connect").addEventListener("click", connectWatch);

    $("#project-name").value = state.project.name || "Untitled";
    setProject(state.project);
    persistLocal();
    draw();
    refreshProjectList();

    if ("serviceWorker" in navigator) {
        navigator.serviceWorker.register("sw.js").catch(() => {});
    }
}

boot();
