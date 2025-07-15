const $chatRoomList = document.getElementById('chatRoomList');
const $chatCategory = document.getElementById('chatCategory');
const $chatCategoryItem = $chatCategory.querySelectorAll(':scope > .chat-list > .category-item');
const $chatViews = document.querySelectorAll('.chat-view');
const $chatStartBtn = document.getElementById('startChatBtn');
const $chatForm = document.getElementById('chatForm');
const $chatBot = document.getElementById('chatbot');
const $chatIcon = $chatBot.querySelector('.chatbot-icon');
const $chatLayout = $chatBot.querySelector('.chatbot-layout');
const $chatCloseBtn = $chatLayout.querySelector(':scope > .chat-intro-view > .intro-header > .close-button');
const $chatFeatureList = $chatLayout.querySelectorAll(':scope > .chat-view.chat-intro-view > .intro-body > .intro-feature-list > .item > .feature-button');
const $chatMessage = document.getElementById('chat-message');
const $chatBackBtn = $chatMessage.querySelector(':scope > .chat-form-header > .chat-back-button');

let currentRoomId = null;

$chatCloseBtn.addEventListener('click', () => {
    $chatLayout.setVisible(false);
    $chatIcon.setVisible(true);
});

$chatIcon.setVisible(true);

$chatIcon.addEventListener('click', () => {
    if ($chatLayout.isVisible()) {
        $chatLayout.setVisible(false);
        $chatIcon.setVisible(true);
    } else {
        $chatLayout.setVisible(true);
        $chatIcon.setVisible(false);
    }
});



$chatForm.onsubmit = async (e) => {
    e.preventDefault();
    await sendChatMessage();
};

async function sendChatMessage() {
    const input = $chatForm.querySelector('input[name="message"]');
    const message = input.value.trim();
    if (!message) {
        dialog.showSimpleOk('오류', '메세지를 입력해주세요.');
        return;
    }

    if (!currentRoomId) {
        dialog.showSimpleOk('오류', '먼저 채팅을 시작해주세요.');
        return;
    }

    appendMessage('user', message);
    input.value = '';
    appendMessage('bot', 'typing');

    try {
        const body = {
            chatRoomId: currentRoomId,
            message: message
        };

        const res = await fetch('/api/chat/register', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(body),
            credentials: 'include'
        });

        const result = await res.json();

        if (result.result === 'FAILURE_SESSION_EXPIRED') {
            dialog.showSimpleOk('오류', '로그인이 만료되었습니다. 다시 로그인해 주세요', () => {
                location.href = `${window.origin}/user/login`;
            });
            return;
        }

        if (result.result === 'failure') {
            removeTypingBubble();
            dialog.showSimpleOk('오류', '챗봇을 이용할 수 없습니다. 잠시 후 다시 시도해주세요.');
            return;
        }

        removeTypingBubble();
        appendMessage('bot', result.payload[1].message);

        try {
            const books = JSON.parse(result.payload[1].payload);
            if (Array.isArray(books)) {
                books.forEach(book => {
                    const bookLine = `"${book.title}" (${book.author}, ${book.publisher})`;
                    appendMessage('bot', bookLine);
                });
            }
        } catch (e) {
            dialog.showSimpleOk('오류', '챗봇 응답을 불러오는데 실패했습니다. 잠시 후 다시 시도해주세요.');
        }

        input.focus();

    } catch (e) {
        removeTypingBubble();
        dialog.showSimpleOk('오류', '알 수 없는 오류로 챗봇을 이용하실 수 없습니다. 잠시 후 다시 시도해주세요.');
    }
}

$chatStartBtn.addEventListener('click', async (e) => {
    e.preventDefault();
    await startNewChatRoom();
});

async function startNewChatRoom() {
    try {
        const res = await fetch('/api/chat/room/register', {
            method: 'POST',
            credentials: 'include'
        });
        const result = await res.json();

        if (result.result === 'FAILURE_SESSION_EXPIRED') {
            dialog.showSimpleOk('오류', '로그인이 만료되었거나 권한이없습니다. 로그인 후 다시 시도해주세요.', () => {
                location.href = `${window.origin}/user/login`;
            });
            return;
        }

        if (result.result === 'FAILURE') {
            dialog.showSimpleOk('오류', '채팅을 시작할 수 없습니다. 잠시 후 다시 시도해 주세요.');
            return;
        }

        currentRoomId = result.payload.id;
        showChatView('main');
        clearMessage();
        appendMessage('bot', '안녕하세요 reBook 챗봇입니다. 무엇을 도와드릴까요?');
    } catch (e) {
        dialog.showSimpleOk('오류', '알 수 없는 이유로 챗봇을 이용할 수 없습니다. 잠시 후 다시 시도해주세요.');
    }
}

function clearMessage() {
    const $list = document.querySelector('#chat-message ul.list');
    if ($list) {
        $list.innerHTML = '';
    }
}

function appendMessage(sender, text) {
    const $list = document.querySelector('#chat-message ul.list');
    if (!$list) return;

    const $item = document.createElement('li');
    $item.className = `message ${sender}`;
    if (text === 'typing' && sender === 'bot') $item.classList.add('typing');

    const $bubble = document.createElement('div');
    $bubble.className = 'bubble';

    if (text === 'typing' && sender === 'bot') {
        const $img = document.createElement('img');
        $img.src = '/chatbot/assets/images/text-bubble.gif';
        $img.alt = 'GPT 응답 중...';
        $img.style.height = '1.25rem';
        $bubble.appendChild($img);
    } else {
        $bubble.textContent = text;
    }

    $item.appendChild($bubble);
    $list.appendChild($item);
    $list.scrollTop = $list.scrollHeight;
}

function removeTypingBubble() {
    const $list = document.querySelector('#chat-message ul.list');
    const $typing = $list.querySelector('li.message.bot.typing');
    if ($typing) $typing.remove();
}

function showChatView(target) {
    $chatViews.forEach($view => {
        $view.setVisible($view.dataset.view === target);
        if (target === 'main') {
            $chatCategory.removeAttribute('data-rb-visible');
        } else {
            $chatCategory.setAttribute('data-rb-visible', '');
        }
    });
}

$chatBackBtn.addEventListener('click', () => {
    $chatViews.forEach($view => {
        $view.setVisible(false);
    });
    const $introView = $chatLayout.querySelector('.chat-view.chat-intro-view');
    $introView.setVisible(true);
    $chatCategory.setVisible(true);
});

$chatFeatureList.forEach($item => {
    $item.addEventListener('click', async () => {
        const target = $item.dataset.target;
        showChatView(target);

        if (target === 'room') {
            await loadChatRooms();
        }
        if (target === 'main') {
            if (!currentRoomId) {
                await startNewChatRoom();
            }
            const input = $chatForm.querySelector('input[name="message"]');
            input.value = '니가 생각하는 오늘 하루의 책 추천 좀 해줘';
            await sendChatMessage();
        }
    });
});

$chatCategoryItem.forEach($item => {
    $item.addEventListener('click', async () => {
        const target = $item.dataset.target;
        showChatView(target);

        if (target === 'room') {
            await loadChatRooms();
        }
    });
});

async function loadChatRooms() {
    $chatRoomList.innerHTML = '';

    try {
        const res = await fetch('/api/chat/room/lists', {
            credentials: 'include'
        });

        const result = await res.json();

        if (result.result === 'FAILURE_SESSION_EXPIRED') {
            dialog.showSimpleOk('오류', '로그인이 만료되었거나 권한이 없습니다. 로그인 후 다시 시도해 주세요.', () => {
                location.href = `${window.origin}/user/login`;
            });
            return;
        }

        if (result.result === 'failure') {
            dialog.showSimpleOk('오류', '채팅방을 불러올 수 없습니다. 잠시 후 다시 시도해 주세요.');
            return;
        }

        const rooms = result.payload;
        if (!rooms.length) {
            const $empty = document.createElement('li');
            $empty.className = 'item';
            $empty.innerText = '새로운 채팅을 시작해보세요';
            $chatRoomList.appendChild($empty);
            return;
        }

        rooms.forEach(room => {
            const $item = document.createElement('li');
            $item.className = 'item';

            const previewText = room.lastMessage || '(최근 메시지 없음)';
            const date = room.lastMessageAt
                ? new Date(room.lastMessageAt).toLocaleString('ko-KR', {
                    month: 'short',
                    day: 'numeric',
                    hour: '2-digit',
                    minute: '2-digit',
                })
                : '';

            $item.innerHTML = `
                <div class="chat-room-preview">
                    <div class="chat-room-title">채팅방 #${room.roomId}</div>
                    <div class="chat-room-message">${previewText}</div>
                    <div class="chat-room-date">${date}</div>
                </div>
            `;

            // 클릭 시 진입 등 이벤트도 추가 가능
            $item.addEventListener('click', async () => {
                currentRoomId = room.roomId;
                showChatView('main');
                clearMessage();
                await loadChatMessage(currentRoomId);
            });

            $chatRoomList.appendChild($item);
        });
    } catch (e) {
        dialog.showSimpleOk('오류', '알 수 없는 오류로인해 채팅을 이용하실 수 없습니다. 잠시 후 다시 시도해주세요.');
    }
}

async function loadChatMessage(roomId) {
    try {
        const res = await fetch(`/api/chat/room/messages?roomId=${roomId}`, {
            method: 'GET',
            credentials: 'include'
        });

        const result = await res.json();
        if (result.result === 'FAILURE_SESSION_EXPIRED') {
            dialog.showSimpleOk('오류', '로그인이 만료되었거나 권한이없습니다. 로그인 후 다시 시도해주세요.', () => {
                location.href = `${window.origin}/user/login`;
            });
            return;
        }
        if (result.result === 'FAILURE') {
            dialog.showSimpleOk('오류', '채팅내역을 불러 올 수 없습니다. 잠시 후 다시 시도해주세요.');
            return;
        }
        const messages = result.payload;
        clearMessage();
        messages.forEach(msg => {
            appendMessage(msg.sender === 'user' ? 'user' : 'bot', msg.message);
        });
    } catch (e) {
        dialog.showSimpleOk('오류', '알 수 없는 오류로인하여 채팅내역을 불러올 수 없습니다. 잠시 후 다시 시도해주세요.');
    }

}