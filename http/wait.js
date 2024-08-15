export function wait(seconds) {
    const now = new Date().getTime();
    const waitUtil = now + seconds * 1000;
    while (new Date().getTime() < waitUtil) {
        // waiting
    }
}