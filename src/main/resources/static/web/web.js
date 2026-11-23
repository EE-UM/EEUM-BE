// 이음 공개 웹 공통 스크립트.
// - 미리듣기: 한 번에 한 곡, 한 곡이 끝나면 화면 순서(사연 노래 → 댓글 노래)대로 다음 곡을 이어서 재생
// - 글보기/커버보기 전환, 바텀시트 열고 닫기, 가입·글쓰기 폼 보조
(() => {
    const player = document.getElementById("player");

    // ── 미리듣기 플레이어 ──
    // 재생 순서: 사연 노래, 그 다음 댓글 노래(작성 순). 커버/글 목록은 같은 댓글을 같은 순서로 그리므로 둘 다 같은 순번을 쓴다.
    const queue = [];
    const assignQueue = () => {
        const hero = document.querySelector(".hero .play[data-src]");
        if (hero) {
            hero.dataset.queueIndex = "0";
            queue.push(hero.dataset.src);
        }
        const offset = queue.length;
        const gridButtons = document.querySelectorAll(".comment-grid .play[data-src]");
        gridButtons.forEach((button, index) => {
            button.dataset.queueIndex = String(offset + index);
            queue.push(button.dataset.src);
        });
        document.querySelectorAll(".comment-list .play[data-src]").forEach((button, index) => {
            button.dataset.queueIndex = String(offset + index);
        });
    };

    let currentSrc = null;
    let currentIndex = -1;

    const syncPlayButtons = () => {
        document.querySelectorAll(".play[data-src]").forEach((button) => {
            const playing = player && !player.paused && button.dataset.src === currentSrc
                && (currentIndex < 0 || button.dataset.queueIndex === undefined
                    || Number(button.dataset.queueIndex) === currentIndex);
            button.classList.toggle("is-playing", playing);
            button.setAttribute("aria-label", playing ? "음악 일시정지" : "음악 재생");
        });
    };

    const revealCurrent = () => {
        if (currentIndex <= 0) return;
        const mode = document.querySelector(".comments")?.dataset.mode ?? "grid";
        const container = mode === "list" ? ".comment-list" : ".comment-grid";
        const button = document.querySelector(`${container} .play[data-queue-index="${currentIndex}"]`);
        button?.scrollIntoView({ behavior: "smooth", block: "center" });
    };

    const play = (src, index) => {
        if (!player) return;
        if (src !== currentSrc) {
            player.src = src;
            currentSrc = src;
        }
        currentIndex = index;
        player.play().catch(syncPlayButtons);
    };

    const toggle = (src, index) => {
        if (src === currentSrc && index === currentIndex && player && !player.paused) {
            player.pause();
            return;
        }
        if (src !== currentSrc || index !== currentIndex) player.currentTime = 0;
        play(src, index);
    };

    const playNext = () => {
        const next = currentIndex + 1;
        if (currentIndex < 0 || next >= queue.length) {
            syncPlayButtons();
            return;
        }
        currentSrc = null; // 같은 곡이 연달아 있어도 처음부터 재생
        play(queue[next], next);
        revealCurrent();
    };

    if (player) {
        ["play", "pause"].forEach((type) => player.addEventListener(type, syncPlayButtons));
        player.addEventListener("ended", playNext);
        assignQueue();
    }

    // 노래 검색 시트 등 다른 스크립트에서 쓰는 단일 플레이어
    window.eeumPlayer = {
        toggle: (src) => toggle(src, -1),
        stop: () => player && player.pause(),
    };

    // ── 바텀시트 ──
    const openSheet = (sheet) => {
        sheet.hidden = false;
        document.body.classList.add("sheet-open");
    };
    const closeSheet = (sheet) => {
        sheet.hidden = true;
        if (!document.querySelector(".sheet:not([hidden])")) {
            document.body.classList.remove("sheet-open");
        }
    };
    window.eeumSheet = { open: openSheet, close: closeSheet };

    document.addEventListener("keydown", (event) => {
        if (event.key !== "Escape") return;
        document.querySelectorAll(".sheet:not([hidden])").forEach(closeSheet);
    });

    // ── 클릭 위임 ──
    document.addEventListener("click", (event) => {
        const playButton = event.target.closest(".play[data-src]");
        if (playButton) {
            event.preventDefault();
            const index = playButton.dataset.queueIndex === undefined ? -1 : Number(playButton.dataset.queueIndex);
            toggle(playButton.dataset.src, index);
            return;
        }
        const viewToggle = event.target.closest("[data-view-toggle]");
        if (viewToggle) {
            const comments = document.querySelector(".comments");
            const next = comments.dataset.mode === "grid" ? "list" : "grid";
            comments.dataset.mode = next;
            viewToggle.dataset.mode = next;
            return;
        }
        const sheetClose = event.target.closest("[data-sheet-close]");
        if (sheetClose) {
            closeSheet(sheetClose.closest(".sheet"));
            return;
        }
        const termsToggle = event.target.closest("[data-terms-toggle]");
        if (termsToggle) {
            const target = document.getElementById(termsToggle.getAttribute("aria-controls"));
            target.hidden = !target.hidden;
            termsToggle.setAttribute("aria-expanded", String(!target.hidden));
            termsToggle.textContent = target.hidden ? "보기" : "접기";
        }
    });

    const viewToggle = document.querySelector("[data-view-toggle]");
    if (viewToggle) viewToggle.dataset.mode = "grid";

    // ── 댓글 입력: 줄 수에 맞춰 높이 조절 ──
    document.querySelectorAll(".comment-row textarea").forEach((textarea) => {
        const resize = () => {
            textarea.style.height = "auto";
            textarea.style.height = `${Math.min(textarea.scrollHeight, 120)}px`;
        };
        textarea.addEventListener("input", resize);
    });

    // ── 가입: 전체 동의 ──
    const signupForm = document.querySelector("[data-signup-form]");
    if (signupForm) {
        const all = signupForm.querySelector("[data-agree-all]");
        const items = [...signupForm.querySelectorAll("[data-agree]")];
        const submit = signupForm.querySelector("[data-signup-submit]");
        const nickname = signupForm.querySelector("[name=nickname]");
        const sync = () => {
            const agreed = items.every((item) => item.checked);
            all.checked = agreed;
            submit.disabled = !agreed || nickname.value.trim().length === 0;
        };
        all.addEventListener("change", () => {
            items.forEach((item) => { item.checked = all.checked; });
            sync();
        });
        items.forEach((item) => item.addEventListener("change", sync));
        nickname.addEventListener("input", sync);
        sync();
    }

    // ── 글쓰기: 글자 수, 마감 방식 시트 ──
    const writeForm = document.querySelector("[data-write-form]");
    if (writeForm) {
        const source = writeForm.querySelector("[data-counter-source]");
        const counter = writeForm.querySelector("[data-counter]");
        const syncCounter = () => { counter.textContent = String(source.value.length); };
        source.addEventListener("input", syncCounter);
        syncCounter();

        const completionSheet = writeForm.querySelector("[data-completion-sheet]");
        const limitSelect = writeForm.querySelector("[data-limit-select]");
        const syncLimit = () => {
            const auto = writeForm.querySelector("[name=completionType]:checked")?.value === "AUTO_COMPLETION";
            limitSelect.disabled = !auto;
        };
        writeForm.querySelectorAll("[name=completionType]").forEach((radio) => radio.addEventListener("change", syncLimit));
        syncLimit();

        writeForm.querySelector("[data-completion-open]").addEventListener("click", () => {
            const title = writeForm.querySelector("[name=title]");
            if (!writeForm.querySelector("[data-music-field=songName]").value) {
                writeForm.querySelector("[data-music-open]").click();
                return;
            }
            if (!title.reportValidity()) return;
            openSheet(completionSheet);
        });
    }
})();
