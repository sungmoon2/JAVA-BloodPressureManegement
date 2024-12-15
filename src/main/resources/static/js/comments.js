$(document).ready(function() {
    const boardId = window.location.pathname.split('/').pop();
    loadComments();

    // 댓글 목록 불러오기
    function loadComments() {
        $.get(`/api/comments/board/${boardId}`)
            .done(function (comments) {
                console.log('Loaded comments:', comments);  // 로딩된 댓글 데이터 확인
                $('#commentsList').empty();
                comments.forEach(comment => {
                    if (!comment.parentId) {
                        console.log('Rendering parent comment:', comment);  // 부모 댓글 렌더링 확인
                        renderComment(comment);
                    }
                });
            })
            .fail(function (error) {
                console.error('댓글 로딩 실패:', error);
            });
    }

    $(document).ready(function () {
        const boardId = window.location.pathname.split('/').pop();
        loadComments();

        // 댓글 목록 불러오기
        function loadComments() {
            console.log('Loading comments for board:', boardId);
            $.get(`/api/comments/board/${boardId}`)
                .done(function (comments) {
                    console.log('Received comments:', comments);
                    const commentsList = $('#commentsList');
                    commentsList.empty();

                    comments.forEach(comment => {
                        if (!comment.parentId) {
                            console.log('Processing parent comment:', comment);
                            renderComment(comment, false);
                        }
                    });
                })
                .fail(function (error) {
                    console.error('Failed to load comments:', error);
                });
        }

        // 댓글 렌더링
        function renderComment(comment, isReply = false) {
            console.log('Rendering comment:', comment, 'isReply:', isReply);

            const template = document.querySelector('#commentTemplate').content.cloneNode(true);
            const commentElement = template.querySelector('.comment-item');

            commentElement.dataset.commentId = comment.id;

            // 작성자 이름 설정
            const authorElement = commentElement.querySelector('.author-name');
            if (authorElement) {
                authorElement.textContent = comment.memberUsername;
            } else {
                console.error(`Could not find author element for comment ${comment.id}`);
            }

            commentElement.querySelector('.comment-date').textContent = formatDate(comment.createdAt);

            const contentElement = commentElement.querySelector('.comment-content');
            console.log('Comment status:', {
                id: comment.id,
                isDeleted: comment.isDeleted,
                content: comment.content
            });

            if (comment.isDeleted) {
                contentElement.textContent = "삭제된 댓글입니다";
                contentElement.classList.add('comment-deleted');

                // 삭제된 댓글의 버튼들과 답글 폼 제거
                commentElement.querySelector('.comment-actions')?.remove();
                commentElement.querySelector('.reply-form-container')?.remove();
            } else {
                contentElement.textContent = comment.content;
                commentElement.dataset.originalContent = comment.content;
            }

            // 현재 로그인한 사용자의 댓글인 경우에만 수정/삭제 버튼 표시
            if (comment.memberUsername !== $('#currentUsername').val()) {
                commentElement.querySelector('.btn-edit')?.remove();
                commentElement.querySelector('.btn-delete')?.remove();
            }

            if (isReply) {
                commentElement.querySelector('.btn-reply')?.remove();
                commentElement.classList.add('reply-item');
            }

            let container;
            if (isReply) {
                container = document.querySelector(`#commentsList .comment-item[data-comment-id="${comment.parentId}"] .replies-list`);
            } else {
                container = document.querySelector('#commentsList');
            }

            if (container) {
                container.appendChild(commentElement);

                // 답글이 있는 경우 재귀적으로 렌더링
                if (comment.children && comment.children.length > 0) {
                    console.log('Rendering children for comment:', comment.id);
                    comment.children.forEach(child => {
                        renderComment(child, true);
                    });
                }
            }
        }

        // 댓글 작성
        $('#submitComment').click(function () {
            const content = $('#commentContent').val().trim();
            if (!content) {
                alert('댓글 내용을 입력해주세요.');
                return;
            }

            $.ajax({
                url: '/api/comments',
                type: 'POST',
                contentType: 'application/json',
                data: JSON.stringify({
                    boardId: boardId,
                    content: content
                })
            })
                .done(function (response) {
                    $('#commentContent').val('');
                    loadComments();
                })
                .fail(function (error) {
                    console.error('댓글 작성 실패:', error);
                    alert('댓글 작성에 실패했습니다.');
                });
        });

        // 답글 폼 토글
        $(document).on('click', '.btn-reply', function () {
            const commentItem = $(this).closest('.comment-item');
            commentItem.find('.reply-form-container').toggle();
        });

        // 답글 작성
        $(document).on('click', '.btn-submit-reply', function () {
            const commentItem = $(this).closest('.comment-item');
            const content = commentItem.find('.reply-input').val().trim();
            const parentId = commentItem.data('comment-id');

            if (!content) {
                alert('답글 내용을 입력해주세요.');
                return;
            }

            $.ajax({
                url: '/api/comments',
                type: 'POST',
                contentType: 'application/json',
                data: JSON.stringify({
                    boardId: boardId,
                    content: content,
                    parentId: parentId
                })
            })
                .done(function (response) {
                    commentItem.find('.reply-input').val('');
                    commentItem.find('.reply-form-container').hide();
                    loadComments();
                })
                .fail(function (error) {
                    console.error('답글 작성 실패:', error);
                    alert('답글 작성에 실패했습니다.');
                });
        });

        // 수정 모드 전환
        $(document).on('click', '.btn-edit', function() {
            const commentItem = $(this).closest('.comment-item');
            const commentId = commentItem.data('comment-id');
            const contentElement = commentItem.find('> .comment-content');
            const currentContent = commentItem.data('originalContent');

            // 다른 열려있는 수정 폼들을 원래 상태로 되돌립니다
            $('.comment-item').each(function() {
                const $item = $(this);
                if ($item.data('comment-id') !== commentId && $item.find('.comment-edit-form').length > 0) {
                    $item.find('> .comment-content').show();
                    $item.find('.comment-edit-form').remove();
                }
            });

            // 현재 댓글/답글의 내용 영역을 숨기고 수정 폼 표시
            contentElement.hide();
            contentElement.after(`
            <div class="comment-edit-form">
                <textarea class="comment-edit-input" data-comment-id="${commentId}">${currentContent}</textarea>
                <div class="comment-form-buttons">
                    <button type="button" class="btn-update-comment btn-primary">수정</button>
                    <button type="button" class="btn-cancel-edit btn-secondary">취소</button>
                </div>
            </div>
        `);
        });

        // 수정 취소
        $(document).on('click', '.btn-cancel-edit', function() {
            const commentItem = $(this).closest('.comment-item');
            const contentElement = commentItem.find('.comment-content');
            const originalContent = commentItem.data('originalContent');
            contentElement.show();
            commentItem.find('.comment-edit-form').remove();
        });

        // 댓글 수정
        $(document).on('click', '.btn-update-comment', function () {
            const commentItem = $(this).closest('.comment-item');
            const commentId = commentItem.data('comment-id');
            const content = commentItem.find('.comment-edit-input').val().trim();
            const contentElement = commentItem.find('.comment-content');

            // 수정 내용을 저장하고 ".edited" 클래스 추가
            contentElement.text(content);
            contentElement.addClass('edited');

            if (!content) {
                alert('댓글 내용을 입력해주세요.');
                return;
            }
            $.ajax({
                url: `/api/comments/${commentId}`,
                type: 'PUT',
                contentType: 'application/json',
                data: JSON.stringify({
                    boardId: boardId,
                    content: content
                })
            })
                .done(function (response) {
                    loadComments();
                })
                .fail(function (error) {
                    console.error('댓글 수정 실패:', error);
                    alert('댓글 수정에 실패했습니다.');
                });
        });

        // 댓글 삭제
        $(document).on('click', '.btn-delete', function () {
            if (!confirm('정말 삭제하시겠습니까?')) {
                return;
            }

            const commentItem = $(this).closest('.comment-item');
            const commentId = commentItem.data('comment-id');

            $.ajax({
                url: `/api/comments/${commentId}`,
                type: 'DELETE'
            })
                .done(function () {
                    loadComments();
                })
                .fail(function (error) {
                    console.error('댓글 삭제 실패:', error);
                    alert('댓글 삭제에 실패했습니다.');
                });
        });

        // 취소 버튼
        $('#cancelComment, .btn-cancel-reply').click(function () {
            $(this).closest('form').find('textarea').val('');
        });

        // 날짜 포맷팅 함수
        function formatDate(dateString) {
            const date = new Date(dateString);
            const year = date.getFullYear();
            const month = String(date.getMonth() + 1).padStart(2, '0');
            const day = String(date.getDate()).padStart(2, '0');
            const hours = String(date.getHours()).padStart(2, '0');
            const minutes = String(date.getMinutes()).padStart(2, '0');

            return `${year}-${month}-${day} ${hours}:${minutes}`;
        }
    });
});