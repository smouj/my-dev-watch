# Samples

Small, human-readable fixtures that can be opened with the CLI:

~~~bash
my-dev-watch capture inspect samples/battery-baseline.mdcap
my-dev-watch battery A2 56
~~~

battery-baseline.mdcap is synthetic: it reproduces the documented A2 request and
the 0x56 (86 %) answer without containing a real device address. Real captures must
be stripped of names and addresses before they are committed.
