(function () {
  const API_URL = "http://localhost:8080/api/ai-brain/chat";
  const SESSION_KEY = "lc-ai-chat-session";
  const OPEN_KEY = "lc-ai-chat-open";
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
            formatText(message.text) +
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

  function formatText(text) {
    return escapeHtml(text).replace(/\n/g, "<br>");
  }

  function escapeHtml(text) {
    return String(text)
      .replace(/&/g, "&amp;")
      .replace(/</g, "&lt;")
      .replace(/>/g, "&gt;")
      .replace(/"/g, "&quot;");
  }
})();
