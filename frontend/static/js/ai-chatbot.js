(function () {
  const API_URL = "http://localhost:8080/api/ai-brain/chat";
  const SESSION_KEY = "lc-ai-chat-session";
  const OPEN_KEY = "lc-ai-chat-open";
  const SIZE_KEY = "lc-ai-chat-size";
  const HISTORY_PREFIX = "lc-ai-chat-history-";
  const WELCOME =
    "Hi — I’m the Leadership Compass assistant. Ask about your survey, development plan, or the five leadership languages.";

  const token = localStorage.getItem("token");
  if (!token) {
    return;
  }

  let conversationId = getOrCreateSessionId();
  let historyKey = HISTORY_PREFIX + conversationId;
  let messages = loadHistory(historyKey);
  let sending = false;

  const root = document.createElement("div");
  root.className = "lc-chat";
  root.innerHTML = `
    <div class="lc-chat-panel" id="lcChatPanel" hidden>
      <div class="lc-chat-resize lc-chat-resize-w" id="lcChatResizeW" role="separator" aria-orientation="vertical" aria-label="Resize chat width"></div>
      <div class="lc-chat-resize lc-chat-resize-n" id="lcChatResizeN" role="separator" aria-orientation="horizontal" aria-label="Resize chat height"></div>
      <div class="lc-chat-header">
        <div>
          <p class="lc-chat-kicker">Leadership Compass</p>
          <h2 class="lc-chat-title">Ask a question</h2>
        </div>
        <div class="lc-chat-header-actions">
          <button type="button" class="lc-chat-text-btn" id="lcChatNew" title="Start a new conversation">New</button>
          <button type="button" class="lc-chat-icon-btn" id="lcChatClose" aria-label="Close chat">
            <svg viewBox="0 0 24 24" aria-hidden="true"><path d="M6 6l12 12M18 6L6 18" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round"/></svg>
          </button>
        </div>
      </div>
      <div class="lc-chat-messages" id="lcChatMessages" role="log" aria-live="polite"></div>
      <form class="lc-chat-form" id="lcChatForm">
        <label class="visually-hidden" for="lcChatInput">Your question</label>
        <textarea id="lcChatInput" rows="1" placeholder="Ask a question..." maxlength="2000" required></textarea>
        <button type="submit" class="lc-chat-send" id="lcChatSend" aria-label="Send">
          <svg viewBox="0 0 24 24" aria-hidden="true"><path d="M4 12l15-8-6 16-2.5-6.5L4 12z" fill="currentColor"/></svg>
        </button>
      </form>
    </div>
    <button type="button" class="lc-chat-fab" id="lcChatFab" aria-label="Open assistant" aria-expanded="false">
      <svg viewBox="0 0 24 24" aria-hidden="true">
        <path d="M5 5h14a2 2 0 0 1 2 2v9a2 2 0 0 1-2 2H9l-4 4v-4H5a2 2 0 0 1-2-2V7a2 2 0 0 1 2-2z" fill="none" stroke="currentColor" stroke-width="1.8" stroke-linejoin="round"/>
      </svg>
    </button>
  `;
  document.body.appendChild(root);

  const panel = document.getElementById("lcChatPanel");
  const fab = document.getElementById("lcChatFab");
  const closeBtn = document.getElementById("lcChatClose");
  const newBtn = document.getElementById("lcChatNew");
  const form = document.getElementById("lcChatForm");
  const input = document.getElementById("lcChatInput");
  const sendBtn = document.getElementById("lcChatSend");
  const messageList = document.getElementById("lcChatMessages");
  const resizeW = document.getElementById("lcChatResizeW");
  const resizeN = document.getElementById("lcChatResizeN");

  applySavedSize();
  renderMessages();
  if (sessionStorage.getItem(OPEN_KEY) === "1") {
    setOpen(true);
  }

  fab.addEventListener("click", function () {
    setOpen(panel.hidden);
  });
  closeBtn.addEventListener("click", function () {
    setOpen(false);
  });
  newBtn.addEventListener("click", startNewConversation);
  form.addEventListener("submit", function (event) {
    event.preventDefault();
    sendMessage();
  });
  input.addEventListener("keydown", function (event) {
    if (event.key === "Enter" && !event.shiftKey) {
      event.preventDefault();
      sendMessage();
    }
  });
  input.addEventListener("input", autosize);
  enableResize(resizeW, "width");
  enableResize(resizeN, "height");
  document.addEventListener("keydown", function (event) {
    if (event.key === "Escape" && !panel.hidden) {
      setOpen(false);
    }
  });

  function setOpen(open) {
    panel.hidden = !open;
    fab.setAttribute("aria-expanded", open ? "true" : "false");
    fab.classList.toggle("is-open", open);
    sessionStorage.setItem(OPEN_KEY, open ? "1" : "0");
    if (open) {
      renderMessages();
      input.focus();
      scrollToBottom();
    }
  }

  function startNewConversation() {
    sessionStorage.removeItem(historyKey);
    conversationId = createSessionId();
    sessionStorage.setItem(SESSION_KEY, conversationId);
    historyKey = HISTORY_PREFIX + conversationId;
    messages = [];
    saveHistory();
    renderMessages();
    input.focus();
  }

  async function sendMessage() {
    const query = input.value.trim();
    if (!query || sending) {
      return;
    }

    const currentToken = localStorage.getItem("token");
    if (!currentToken) {
      window.location.href = "index.html";
      return;
    }

    messages.push({ role: "user", text: query });
    saveHistory();
    input.value = "";
    autosize();
    sending = true;
    sendBtn.disabled = true;
    renderMessages(true);

    try {
      const response = await fetch(API_URL, {
        method: "POST",
        headers: {
          "Content-Type": "application/json",
          Authorization: "Bearer " + currentToken
        },
        body: JSON.stringify({
          query: query,
          conversationId: conversationId
        })
      });

      if (response.status === 401 || response.status === 403) {
        throw new Error("Please log in again to use the assistant.");
      }
      if (response.status === 429) {
        throw new Error("You're sending messages too quickly. Please wait a few minutes and try again.");
      }
      if (response.status === 503) {
        throw new Error("The assistant is temporarily unavailable.");
      }
      if (!response.ok) {
        throw new Error("I couldn’t get an answer right now. Please try again.");
      }

      const data = await response.json();
      const answer = (data && data.answer ? String(data.answer) : "").trim();
      if (!answer) {
        throw new Error("I couldn’t get an answer right now. Please try again.");
      }
      messages.push({ role: "assistant", text: answer });
      saveHistory();
    } catch (error) {
      messages.push({
        role: "error",
        text: error.message || "Unable to reach the assistant."
      });
    } finally {
      sending = false;
      sendBtn.disabled = false;
      renderMessages();
      input.focus();
    }
  }

  function renderMessages(showTyping) {
    if (!messages.length) {
      messageList.innerHTML =
        '<div class="lc-chat-welcome">' + escapeHtml(WELCOME) + "</div>";
    } else {
      messageList.innerHTML = messages
        .map(function (message) {
          const css =
            message.role === "user"
              ? "is-user"
              : message.role === "error"
                ? "is-error"
                : "is-assistant";
          return (
            '<div class="lc-chat-bubble ' +
            css +
            '">' +
            (message.role === "assistant" ? formatMarkdown(message.text) : formatPlain(message.text)) +
            "</div>"
          );
        })
        .join("");
    }

    if (showTyping) {
      messageList.insertAdjacentHTML(
        "beforeend",
        '<div class="lc-chat-bubble is-assistant is-typing" aria-label="Assistant is typing"><span></span><span></span><span></span></div>'
      );
    }
    scrollToBottom();
  }

  function loadHistory(key) {
    try {
      const raw = sessionStorage.getItem(key);
      const parsed = raw ? JSON.parse(raw) : [];
      return Array.isArray(parsed) ? parsed.slice(-40) : [];
    } catch (error) {
      return [];
    }
  }

  function saveHistory() {
    sessionStorage.setItem(historyKey, JSON.stringify(messages.slice(-40)));
  }

  function getOrCreateSessionId() {
    const existing = sessionStorage.getItem(SESSION_KEY);
    if (existing && /^[A-Za-z0-9_-]{1,64}$/.test(existing)) {
      return existing;
    }
    const created = createSessionId();
    sessionStorage.setItem(SESSION_KEY, created);
    return created;
  }

  function createSessionId() {
    if (window.crypto && typeof window.crypto.randomUUID === "function") {
      return window.crypto.randomUUID();
    }
    return "session-" + Date.now();
  }

  function autosize() {
    input.style.height = "auto";
    input.style.height = Math.min(input.scrollHeight, 120) + "px";
  }

  function scrollToBottom() {
    messageList.scrollTop = messageList.scrollHeight;
  }

  function applySavedSize() {
    const size = loadSize();
    if (size.width) {
      panel.style.width = size.width + "px";
    }
    if (size.height) {
      panel.style.height = size.height + "px";
    }
  }

  function loadSize() {
    try {
      const parsed = JSON.parse(sessionStorage.getItem(SIZE_KEY) || "{}");
      return parsed && typeof parsed === "object" ? parsed : {};
    } catch (error) {
      return {};
    }
  }

  function saveSize() {
    sessionStorage.setItem(
      SIZE_KEY,
      JSON.stringify({
        width: Math.round(panel.getBoundingClientRect().width),
        height: Math.round(panel.getBoundingClientRect().height)
      })
    );
  }

  function enableResize(handle, axis) {
    if (!handle) {
      return;
    }
    handle.addEventListener("mousedown", function (event) {
      event.preventDefault();
      const startX = event.clientX;
      const startY = event.clientY;
      const startWidth = panel.getBoundingClientRect().width;
      const startHeight = panel.getBoundingClientRect().height;
      const minWidth = 320;
      const minHeight = 360;
      const maxWidth = Math.max(minWidth, window.innerWidth - 48);
      const maxHeight = Math.max(minHeight, window.innerHeight - 96);

      function onMove(moveEvent) {
        if (axis === "width") {
          const nextWidth = Math.min(
            maxWidth,
            Math.max(minWidth, startWidth + (startX - moveEvent.clientX))
          );
          panel.style.width = nextWidth + "px";
        } else {
          const nextHeight = Math.min(
            maxHeight,
            Math.max(minHeight, startHeight + (startY - moveEvent.clientY))
          );
          panel.style.height = nextHeight + "px";
        }
      }

      function onUp() {
        document.removeEventListener("mousemove", onMove);
        document.removeEventListener("mouseup", onUp);
        document.body.classList.remove("lc-chat-resizing");
        saveSize();
      }

      document.body.classList.add("lc-chat-resizing");
      document.addEventListener("mousemove", onMove);
      document.addEventListener("mouseup", onUp);
    });
  }

  function formatPlain(text) {
    return escapeHtml(text).replace(/\n/g, "<br>");
  }

  function formatMarkdown(text) {
    const lines = escapeHtml(text).split(/\n/);
    let html = "";
    let inList = false;

    lines.forEach(function (line) {
      const bullet = line.match(/^\s*[\*\-]\s+(.*)$/);
      if (bullet) {
        if (!inList) {
          html += '<ul class="lc-chat-list">';
          inList = true;
        }
        html += "<li>" + formatInline(bullet[1]) + "</li>";
        return;
      }
      if (inList) {
        html += "</ul>";
        inList = false;
      }
      if (line.trim() === "") {
        html += "<br>";
        return;
      }
      html += formatInline(line) + "<br>";
    });
    if (inList) {
      html += "</ul>";
    }
    return html.replace(/(<br>)+$/, "");
  }

  function formatInline(text) {
    return text
      .replace(/\*\*(.+?)\*\*/g, "<strong>$1</strong>")
      .replace(/__([^_]+?)__/g, "<strong>$1</strong>");
  }

  function escapeHtml(text) {
    return String(text)
      .replace(/&/g, "&amp;")
      .replace(/</g, "&lt;")
      .replace(/>/g, "&gt;")
      .replace(/"/g, "&quot;");
  }
})();
