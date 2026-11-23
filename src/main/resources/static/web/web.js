// 이음 공개 웹 — 미리듣기 재생(한 번에 한 곡)과 글보기/커버보기 전환.
(() => {
    const player = document.getElementById("player");
    let currentSrc = null;

    const syncPlayButtons = () => {
        document.querySelectorAll(".play[data-src]").forEach((button) => {
            const playing = !player.paused && button.dataset.src === currentSrc;
            button.classList.toggle("is-playing", playing);
            button.setAttribute("aria-label", playing ? "음악 일시정지" : "음악 재생");
        });
    };

    const togglePlay = (src) => {
        if (src === currentSrc && !player.paused) {
            player.pause();
            return;
        }
        if (src !== currentSrc) {
            player.src = src;
            currentSrc = src;
        }
        player.play().catch(syncPlayButtons);
    };

    const toggleView = (button) => {
        const comments = document.querySelector(".comments");
        const next = comments.dataset.mode === "grid" ? "list" : "grid";
        comments.dataset.mode = next;
        button.dataset.mode = next;
    };

    if (player) {
        ["play", "pause", "ended"].forEach((type) => player.addEventListener(type, syncPlayButtons));
    }

    const viewToggle = document.querySelector("[data-view-toggle]");
    if (viewToggle) viewToggle.dataset.mode = "grid";

    document.addEventListener("click", (event) => {
        const playButton = event.target.closest(".play[data-src]");
        if (playButton && player) {
            togglePlay(playButton.dataset.src);
            return;
        }
        const toggle = event.target.closest("[data-view-toggle]");
        if (toggle) toggleView(toggle);
    });
})();
