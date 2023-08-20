window.onload = () => {
    if (window.location.href.match('comment.html') == null) {
        $(function () {
            $.ajax({
                type: "GET",
                url: "php/getAllMovies.php",  //php file located on the server.
                success: (resp) => {
                    let size = resp.size
                    console.log(size)
                    console.log(resp)
                    if (size < 0)
                        window.alert("An error occurred!")
                    else if (size === 0)
                        window.alert("Movie table are empty")
                    else createMoviesContent(resp)
                }
            });
        });
    }
}

function createMoviesContent(responseJson) {
    let movies = document.getElementById('movies')
    let listOfMovies = responseJson["movies"]
    for (let movie of listOfMovies) {
        movies.appendChild(convertMovieToHtml(movie))
    }
}

function convertMovieToHtml(movie) {
    let movieDiv = document.createElement("div")
    movieDiv.setAttribute("class", "col mb-5")
    let movieContentDiv = document.createElement("div")
    movieContentDiv.setAttribute("class", "card h-100")
    let imageElement = document.createElement("img")
    imageElement.src = movie["image_url"]  // read from json
    imageElement.setAttribute("class", "card-img-top")
    imageElement.setAttribute("width", "100px")
    imageElement.setAttribute("height", "300px")
    let nameDiv = document.createElement("div")
    nameDiv.setAttribute("class", "card-body p-4")
    let directorDiv = document.createElement("div")
    directorDiv.setAttribute("class", "text-center")
    let filmName = document.createElement("h5")
    filmName.setAttribute("class", "fw-bolder")
    const filmTextNode = document.createTextNode(movie["name"])           // read from json
    const directorTextNode = document.createTextNode(movie["director"])   // read from json
    directorDiv.appendChild(filmName)
    filmName.appendChild(filmTextNode)
    directorDiv.appendChild(directorTextNode)
    let viewCommentDiv = document.createElement("div")
    viewCommentDiv.setAttribute("class", "card-footer p-4 pt-0 border-top-0 bg-transparent")
    let viewButtonDiv = document.createElement("div")
    viewButtonDiv.setAttribute("class", "text-center")
    let viewButtonA = document.createElement("a")
    viewButtonA.setAttribute("class", "btn btn-outline-dark mt-auto")
    viewButtonA.addEventListener('click', () => readComments(movie["name"]))
    const viewButtonTextNode = document.createTextNode("View comments")
    viewButtonA.appendChild(viewButtonTextNode)
    let usernameLabel = document.createElement("label")
    usernameLabel.setAttribute("for", "username")
    usernameLabel.innerHTML = "username :"
    let usernameBox = document.createElement("input")
    usernameBox.setAttribute("id", "username_".concat(movie["name"]))
    usernameBox.setAttribute("name", "username")
    usernameBox.setAttribute("type", "text")
    let selectBox = document.createElement("select")
    selectBox.setAttribute("name", "language")
    selectBox.setAttribute("id", "language_".concat(movie["name"]))
    let lang_1 = document.createElement("option")
    let lang_1_text = document.createTextNode("English")
    lang_1.setAttribute("value", "English")
    lang_1.appendChild(lang_1_text)
    selectBox.appendChild(lang_1)
    let lang_2 = document.createElement("option")
    let lang_2_text = document.createTextNode("German")
    lang_2.setAttribute("value", "German")
    lang_2.appendChild(lang_2_text)
    selectBox.appendChild(lang_2)
    let lang_3 = document.createElement("option")
    let lang_3_text = document.createTextNode("Spanish")
    lang_3.setAttribute("value", "Spanish")
    lang_3.appendChild(lang_3_text)
    selectBox.appendChild(lang_3)
    let sendFile = document.createElement("input")
    sendFile.setAttribute("class", "form-control")
    sendFile.setAttribute("id", "input_".concat(movie["name"]))
    sendFile.setAttribute("type", "file")
    sendFile.setAttribute("name", "voiceUpload")
    let submitButton = document.createElement("button")
    submitButton.setAttribute("type", "button")
    submitButton.setAttribute("class", "btn btn-secondary")
    const submitButtonTextNode = document.createTextNode("Submit comment")
    submitButton.appendChild(submitButtonTextNode)
    submitButton.addEventListener('click', () => uploadFile(movie["name"]))
    movieDiv.appendChild(movieContentDiv)
    movieContentDiv.appendChild(imageElement)
    movieContentDiv.appendChild(nameDiv)
    nameDiv.appendChild(directorDiv)
    movieContentDiv.appendChild(viewCommentDiv)
    viewCommentDiv.appendChild(viewButtonDiv)
    viewButtonDiv.appendChild(viewButtonA)
    movieContentDiv.appendChild(usernameLabel)
    movieContentDiv.appendChild(usernameBox)
    movieContentDiv.appendChild(selectBox)
    movieContentDiv.appendChild(sendFile)
    movieContentDiv.appendChild(submitButton)
    return movieDiv
}

function readComments(filmName) {
    var selectedValue = document.getElementById("language_".concat(filmName))
    var username = document.getElementById("username_".concat(filmName))
    var queryParam = "?film=" + filmName + "&language=" + selectedValue.value + "&username=" + username.value
    $(function () {
        $.ajax({
            type: "GET",
            url: "php/getFilmComments.php".concat(queryParam),
            success: (resp) => {
                let size = resp.size
                console.log(resp)
                if (size < 0)
                    window.alert("An error occurred!")
                else if (size === 0)
                    window.alert("Comments table is empty")
                else addCommentsToContent(resp)
            }
        });
    });
}

function addCommentsToContent(comments) {
    let commentsElement = document.getElementById('comments')
    while (commentsElement.firstChild) {
        commentsElement.removeChild(commentsElement.lastChild);
    }
    let listOfComments = comments["comments"]
    for (let comment of listOfComments) {
        commentsElement.appendChild(convertCommentToHtml(comment))
    }
}

function convertCommentToHtml(comment) {
    let fieldText = document.createElement("fieldset")
    fieldText.setAttribute("class", "border")
    let legend = document.createElement("legend")
    legend.setAttribute("class", "text-center")
    const legendTextNode = document.createTextNode(comment["username"])
    legend.appendChild(legendTextNode)
    fieldText.appendChild(legend)
    let textBlock = document.createElement("p")
    const commentTextNode = document.createTextNode(comment["text"])
    textBlock.appendChild(commentTextNode)
    fieldText.appendChild(textBlock)
    return fieldText
}

function uploadFile(filmName) {
    let fileInputElement = document.getElementById("input_" + filmName)
    let usernameElement = document.getElementById("username_" + filmName)
    let bodyFormData = new FormData();
    if (fileInputElement.files[0] != null && usernameElement.value != '') {
        let voice = fileInputElement.files[0]
        bodyFormData.append("file", voice)
        bodyFormData.append("username", usernameElement.value)
        bodyFormData.append("film", filmName);
        axios({
            method: "post",
            url: "php/uploadFile.php",
            data: bodyFormData,
            headers: { "Content-Type": "multipart/form-data" },
        })
            .then(function (response) {
                window.alert(response.data.result)
                console.log(response.data.result);
            })
            .catch(function (response) {
                window.alert("Your comment not allowed to add")
                console.log(response.data);
            });
    } else {
        window.alert("username or input file is empty")
    }
}