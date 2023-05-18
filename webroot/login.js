let login_btn = document.getElementById('login_btn')

// 提示框
function message_box(words, color, time) {
    let messageBox = document.getElementById("message-box");
    messageBox.innerText = words
    messageBox.style.display = "block";
    messageBox.style.backgroundColor = color;
    setTimeout(function () {
        messageBox.style.display = "none";
    }, time);
}

// 表单提交，fetch
function save_user_detail(username, password) {
    let optionObj = {
        method: "POST",
    }
    fetch('/save?username=' + username + "&password=" + password, optionObj)
        // .then(response => response.json())
        .then(data => {
            console.log(data);
        })
        .catch(e => {
            console.log(e);
        })
}

login_btn.addEventListener("mouseover", () => {
    login_btn.style.cursor = "pointer"
    login_btn.style.backgroundColor = "rgba(0, 0, 0, 0.6)"
})

login_btn.addEventListener("mouseout", () => {
    login_btn.style.backgroundColor = "black"
})


login_btn.addEventListener("mousedown", () => {
    login_btn.style.backgroundColor = "rgba(0, 0, 0, 0.3)"
})
login_btn.addEventListener("mouseup", () => {
    login_btn.style.backgroundColor = "rgba(0, 0, 0, 0.8)"
    let username = document.getElementById('username').value
    let password = document.getElementById('password').value
    if (username !== "" && password !== "") {
        message_box('save username and password success!', 'springgreen', 3000)
        save_user_detail(username, password)
    } else {
        message_box('username and password can not be empty!', 'red', 3000)
    }
    console.log("username = %s\npassword = %s\n", username, password)
})

