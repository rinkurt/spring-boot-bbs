function post(parentId, type, contentContainer) {
    let content = contentContainer.val();
    if (!content) {
        alert("不能回复空内容");
        return
    }
    $.ajax({
        type: "POST",
        url: "/comment",
        contentType: "application/json",
        data: JSON.stringify({
            "parentId": parentId,
            "content": content,
            "type": type
        }),
        success: function (response) {
            if (response.code === 200) {
                // Success
                contentContainer.val("");
                window.location.reload();
            } else {
                if (response.code === 2002) {
                    // No login
                    let accepted = confirm(response.message);
                    if (accepted) {
                        window.open("/login");
                        window.localStorage.setItem("closable", "1");
                    }
                } else {
                    alert(response.message);
                }
            }
            // console.log(response);
        },
        dataType: "json"
    });
}

function postComment() {
    let parentId = $("#question_id").val();
    let contentContainer = $("#comment_content");
    post(parentId, 1, contentContainer);
}

function postSubComment(parentId) {
    let contentContainer = $("#comment_input_" + parentId);
    post(parentId, 2, contentContainer);
}

function incLike(e, id, type, receiveId) {
    let liked = e.classList.contains("active");
    $.ajax({
        type: "POST",
        url: "/like",
        contentType: "application/json",
        data: JSON.stringify({
            "id": id,
            "type": type,
            "receiveId": receiveId,
            "liked": liked
        }),
        success: function (response) {
            let element = document.getElementById("like_comment_" + id);
            oldCount = parseInt(element.innerHTML);
            if (response.code === 200) {
                // Success
                e.classList.toggle("active");
                element.innerHTML = oldCount + 1;
            } else if (response.code === 202) {
                e.classList.toggle("active");
                element.innerHTML = oldCount - 1;
            }
        },
        dataType: "json"
    });
}

function getSubComment(e, id) {
    let comments = $("#collapse_" + id);
    let collapse = e.getAttribute("data-collapse");
    if (collapse) {
        comments.removeClass("in");
        e.removeAttribute("data-collapse");
        e.classList.remove("active");
    } else {
        $.getJSON("/comment/" + id, function (data) {
            let subContainer = $("#sub_container_" + id);
            if (subContainer.children().length === 0) {
                $.each(data.data, function (index, comment) {

                    var mediaLeftElement = $("<div/>", {
                        "class": "media-left"
                    }).append($("<img/>", {
                        "class": "media-object media-avatar img-rounded",
                        "src": comment.user.avatarUrl
                    }));

                    var mediaBodyElement = $("<div/>", {
                        "class": "media-body"
                    }).append($("<span/>", {
                        "class": "media-heading text-name",
                        "html": comment.user.name
                    })).append($("<br>")).append($("<span/>", {
                        "html": comment.content
                    })).append($("<div/>", {
                        "class": "text-name a-grey"
                    }).append($("<span/>", {
                        "style": "font-size: 12px;",
                        "html": moment(comment.gmtCreate).format('YYYY-MM-DD')
                    })));

                    var mediaElement = $("<div/>", {
                        "class": "media"
                    }).append(mediaLeftElement).append(mediaBodyElement);

                    subContainer.append(mediaElement);
                });
            }
            comments.addClass("in");
            e.setAttribute("data-collapse", "in");
            e.classList.add("active");
        });
    }
}