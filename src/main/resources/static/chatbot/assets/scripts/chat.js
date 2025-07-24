const $chatRoomList = document.getElementById('chatRoomList');
const $chatCategory = document.getElementById('chatCategory');
const $chatCategoryItem = $chatCategory.querySelectorAll(':scope > .chat-list > .category-item');
const $chatViews = document.querySelectorAll('.chat-view');
const $chatStartBtn = document.getElementById('startChatBtn');
const $chatForm = document.getElementById('chatForm');
const $chatBot = document.getElementById('chatbot');
const $chatIcon = $chatBot.querySelector('.chatbot-icon');
const $chatLayout = $chatBot.querySelector('.chatbot-layout');
const $chatCloseBtn = document.querySelectorAll('.chat-close-button');
const $chatFeatureList = $chatLayout.querySelectorAll(':scope > .chat-view.chat-intro-view > .intro-body > .intro-feature-list > .item > .feature-button');
const $chatMessage = document.getElementById('chat-message');
const $chatBackBtn = $chatMessage.querySelector(':scope > .chat-form-header > .chat-back-button');
const $infoItems = document.querySelectorAll('.chat-room-info .info-list .item');
const CSRF_TOKEN  = document.querySelector('meta[name="_csrf"]').content;
const CSRF_HEADER = document.querySelector('meta[name="_csrf_header"]').content;

let currentRoomId = null;
let silentSend = false;

$chatCloseBtn.forEach(($cbt) => {
    $cbt.addEventListener('click', () => {
        $chatLayout.setVisible(false);
        $chatIcon.setVisible(true);
    });
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

    if (!silentSend) {
        appendMessage('user', message);
    }
    input.value = '';
    appendMessage('bot', 'typing');

    try {
        const body = {
            chatRoomId: currentRoomId,
            message: message
        };

        const res = await fetch('/api/chat/register', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
                [CSRF_HEADER]: CSRF_TOKEN,
            },
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
        silentSend = false;
        appendMessage('bot', result.payload[1].message);

        try {
            const books = JSON.parse(result.payload[1].payload);
            if (Array.isArray(books)) {
                books.forEach(book => {
                    const bookLine = `"${book.title}"`;
                    const $li    = appendMessage('bot', bookLine);
                    if (!$li) {
                        return;
                    }
                    $li.dataset.id = book.id;
                    $li.classList.add('clickable');
                    $li.addEventListener('click', () => {
                        location.href = `${window.origin}/book/?id=${book.id}`;
                    });


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
    if ($chatStartBtn.classList.contains('danger-text')) {
        location.href = `${window.origin}/user/login`;
        return;
    }
    await startNewChatRoom();
});

async function startNewChatRoom(skipGreeting = false) {
    try {
        const res = await fetch('/api/chat/room/register', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
                [CSRF_HEADER]: CSRF_TOKEN,
            },
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
        if (!skipGreeting) {
            appendMessage('bot', '안녕하세요 reBook 챗봇입니다. 무엇을 도와드릴까요?');
        }
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

function appendMessage(sender, text, link = null) {
    const $list = document.querySelector('#chat-message ul.list');
    if (!$list) return null;

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
        $img.style.backgroundColor = '#f1f1f1';
        $bubble.appendChild($img);
    } else {
        const $textNode = document.createElement('p');
        $textNode.textContent = text;
        $bubble.appendChild($textNode);

        if (link) {
            const $btn = document.createElement('button');
            $btn.className = 'chat-link-button';
            $btn.textContent = link.label;
            $btn.addEventListener('click', () => {
                location.href = `${window.origin}${link.href}`;
            });
            $bubble.appendChild($btn);
        }
    }

    $item.appendChild($bubble);
    $list.appendChild($item);
    $list.scrollTop = $list.scrollHeight;
    return $item;
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
            await startNewChatRoom(true);
            const input = $chatForm.querySelector('input[name="message"]');
            input.value = '니가 생각하는 오늘 하루의 책 추천 좀 해줘';
            silentSend = true;
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
<button class="chat-room-delete-button" id="chat-room-delete-button">&times;</button>
                <div class="chat-room-preview">
                    <div class="chat-room-title">채팅방 #${room.roomId}</div>
                    <div class="chat-room-message">${previewText}</div>
                    <div class="chat-room-date">${date}</div>
                </div>
            `;
            const $deleteCR = $item.querySelector('.chat-room-delete-button');
            $deleteCR.addEventListener('click', async (e) => {
                e.stopPropagation();
                dialog.show({
                    title: '삭제',
                    content: `${room.roomId}` + '채팅방을 정말 삭제하시겠습니까?',
                    buttons: [
                        {
                            caption: '취소',
                            color: 'gray',
                            onClickCallback: ($modal) => {
                                dialog.hide($modal);
                            }
                        },
                        {
                            caption: '확인',
                            color: 'blue',
                            onClickCallback: ($modal) => {
                                dialog.hide($modal);
                                deleteChatRoom(room.roomId);
                            }
                        }
                    ]
                });
                return;
            });

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
        console.log(result.payload);
        clearMessage();
        messages.forEach(msg => {
            console.log('[클릭 가능 여부 체크]', msg.message, msg.id);

            const $li = appendMessage(msg.sender === 'user' ? 'user' : 'bot', msg.message);
            if (!$li) {
                return;
            }
            $li.dataset.id = msg.id;
            $li.addEventListener('click', () => {
                location.href = `${window.origin}/book?id=${msg.id}`;
            });

        });

    } catch (e) {
        dialog.showSimpleOk('오류', '알 수 없는 오류로인하여 채팅내역을 불러올 수 없습니다. 잠시 후 다시 시도해주세요.');
    }
}

async function deleteChatRoom(roomId) {
    try {
        const res = await fetch(`/api/chat/room/delete?roomId=${roomId}`, {
            method: 'DELETE',
            credentials: 'include'
        });
        const result = await res.json();
        console.log(result);
        if (result.result === 'FAILURE_SESSION_EXPIRED') {
            dialog.showSimpleOk('오류', '로그인이 만료되었거나 권한이 없습니다. 잠시 후 다시 시도해주세요.', () => {
                location.href = `${window.origin}/user/login`;
            });
            return;
        }
        if (result.result === 'FAILURE') {
            dialog.showSimpleOk('오류', '채팅방을 삭제하지 못하였습니다. 잠시 후 다시 시도해주세요.');
            return;
        }
        if (result.result === 'SUCCESS') {
            alert('zz');
            dialog.showSimpleOk('삭제', '채팅방을 삭제하였습니다.');
            await loadChatRooms();

        }
    } catch (e) {
        dialog.showSimpleOk('오류', '알 수 없는 오류로인하여 채팅방을 삭제하지 못하였습니다. 잠시 후 다시 시도해주세요.');
    }
}

const help_answers = {
    service: `Re:Book은 사용자의 선호와 리뷰 데이터를 기반으로
도서를 추천하고, 챗봇·키워드 검색·리뷰 기능을 지원하는
AI 서점 서비스입니다.`,

    recommend: `추천 로직은
1) 리뷰 수,
2) 평균 평점,
3) 사용자 선호 카테고리,
4) GPT 연관 키워드
를 종합하여 순위를 매깁니다.`,

    chatbot: `챗봇은 OpenAI GPT‑4o-mini API를 사용합니다.
대화 내역과 키워드 입력을 컨텍스트로 보내고,
책 메타데이터를 결합해 답변을 생성합니다.`,

    privacy: `모든 개인정보는 암호화되어 저장되며,
결제·주문 정보를 포함한 민감 데이터는
국내 클라우드 KISA 인증 기준을 준수합니다. 챗봇 대화 이력은 Re:Book 서비스의 원할한 챗봇 서비스를 위하여 사용자들의 대화 데이터들을 임시 저장 및 활용할 수 있습니다.`,

    delete: {
        message: `마이페이지 > 회원탈퇴 메뉴에서 즉시 탈퇴할 수 있습니다.
회원 탈퇴시 복구가 불가능하며, 그 이후에는 데이터가 완전히 삭제됩니다.`,
        action: {
            label: '🔗 회원 탈퇴하러 가기',
            href: `/user/remove-account`
        }
    },
};
$infoItems.forEach($item => {
    $item.addEventListener('click', () => {
        const key = $item.dataset.key;
        const entry = help_answers[key];
        if (!key || !entry) return;

        showChatView('main');
        clearMessage();
        appendMessage('user', $item.textContent.trim());
        appendMessage('bot', 'typing');

        setTimeout(() => {
            removeTypingBubble();
            if (typeof entry === 'string') {
                appendMessage('bot', entry);
            } else {
                appendMessage('bot', entry.message, entry.action);
            }
        }, 600);
    });
});
