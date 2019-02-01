function magnitude(w) {
    return Math.sqrt((w.x * w.x) + (w.y * w.y) + (w.z * w.z));
}

magnitude({
    x: 34,
    y: 12,
    z: 6
});