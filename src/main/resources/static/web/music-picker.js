// 노래 검색 바텀시트 (앱 SearchScreen). 기존 공개 API GET apple-music/search 를 호출하고,
// 고른 곡을 data-music-form 안의 hidden 필드(WebMusic)에 채운다.
(() => {
    const sheet = document.querySelector("[data-music-sheet]");
    const form = document.querySelector("[data-music-form]");
    if (!sheet || !form) return;

    const FIELDS = ["albumName", "songName", "artistName", "artworkUrl", "previewMusicUrl"];
    const searchUrl = sheet.dataset.searchUrl;
    const query = sheet.querySelector("[data-music-query]");
    const results = sheet.querySelector("[data-music-results]");
    const status = sheet.querySelector("[data-music-status]");
    const field = (name) => form.querySelector(`[data-music-field="${name}"]`);

    let debounceTimer = null;
    let searchSeq = 0;

    const setMusic = (music) => {
        FIELDS.forEach((name) => { field(name).value = music ? (music[name] ?? "") : ""; });

        const chip = form.querySelector("[data-music-chip]");
        if (chip) chip.hidden = !music;
        const label = form.querySelector("[data-music-chip-label]");
        if (label) {
            label.textContent = music ? `${music.songName} · ${music.artistName}` : (label.dataset.placeholder ?? "");
        }
        const art = form.querySelector("[data-music-art]");
        if (art) {
            art.hidden = !music?.artworkUrl;
            if (music?.artworkUrl) art.src = music.artworkUrl;
        }
        const submit = form.querySelector("[data-music-submit]");
        if (submit) submit.disabled = !music;
    };

    const currentMusic = () => {
        if (!field("songName").value) return null;
        return Object.fromEntries(FIELDS.map((name) => [name, field(name).value]));
    };

    const open = () => {
        window.eeumSheet.open(sheet);
        setTimeout(() => query.focus(), 50);
    };

    const close = () => {
        window.eeumPlayer.stop();
        window.eeumSheet.close(sheet);
    };

    const renderResults = (items) => {
        results.replaceChildren();
        items.forEach((item) => {
            const li = document.createElement("li");
            li.className = "result-row";

            const pick = document.createElement("button");
            pick.type = "button";
            pick.className = "result-pick";
            pick.addEventListener("click", () => {
                setMusic(item);
                close();
            });

            const art = document.createElement(item.artworkUrl ? "img" : "span");
            art.className = "result-art";
            if (item.artworkUrl) {
                art.src = item.artworkUrl;
                art.alt = "";
                art.loading = "lazy";
            }

            const text = document.createElement("span");
            text.className = "result-text";
            const song = document.createElement("span");
            song.className = "result-song";
            song.textContent = item.songName;
            const artist = document.createElement("span");
            artist.className = "result-artist";
            artist.textContent = item.artistName;
            text.append(song, artist);
            pick.append(art, text);
            li.append(pick);

            if (item.previewMusicUrl) {
                const preview = document.createElement("button");
                preview.type = "button";
                preview.className = "play play-xs";
                preview.dataset.src = item.previewMusicUrl;
                preview.setAttribute("aria-label", "음악 재생");
                preview.innerHTML = '<svg class="icon-play" viewBox="0 0 24 24" aria-hidden="true"><path d="M8 5.5v13l11-6.5z"/></svg>'
                    + '<svg class="icon-pause" viewBox="0 0 24 24" aria-hidden="true"><path d="M7 5h3.5v14H7zM13.5 5H17v14h-3.5z"/></svg>';
                li.append(preview);
            }
            results.append(li);
        });
    };

    const search = async (term) => {
        const seq = ++searchSeq;
        status.textContent = "찾는 중…";
        try {
            const url = `${searchUrl}?term=${encodeURIComponent(term)}&types=songs&limit=20`;
            const response = await fetch(url, { headers: { Accept: "application/json" } });
            if (seq !== searchSeq) return;
            if (response.status === 429) {
                status.textContent = "검색이 많아요. 잠시 후 다시 시도해 주세요.";
                results.replaceChildren();
                return;
            }
            if (!response.ok) throw new Error(String(response.status));
            const body = await response.json();
            if (seq !== searchSeq) return;
            const items = (body.data ?? []).filter((item) => item.songName && item.artistName);
            renderResults(items);
            status.textContent = items.length === 0 ? "검색 결과가 없어요" : "";
        } catch (e) {
            if (seq !== searchSeq) return;
            results.replaceChildren();
            status.textContent = "검색하지 못했어요. 잠시 후 다시 시도해 주세요.";
        }
    };

    query.addEventListener("input", () => {
        clearTimeout(debounceTimer);
        const term = query.value.trim();
        if (term.length === 0) {
            searchSeq++;
            results.replaceChildren();
            status.textContent = "";
            return;
        }
        debounceTimer = setTimeout(() => search(term), 300);
    });
    query.addEventListener("keydown", (event) => {
        if (event.key !== "Enter") return;
        event.preventDefault();
        clearTimeout(debounceTimer);
        const term = query.value.trim();
        if (term) search(term);
    });

    form.querySelectorAll("[data-music-open]").forEach((button) => button.addEventListener("click", open));
    form.querySelector("[data-music-clear]")?.addEventListener("click", () => setMusic(null));

    // 검증 실패로 폼이 다시 그려졌을 때 이전에 고른 곡을 복원한다
    setMusic(currentMusic());
})();
