const apiUrl = 'https://localhost:4567';

function login(username, password) {
    let credentials = 'Basic ' + btoa(username + ':' + password);
    
    fetch(apiUrl + '/sessions', {
        method: 'POST',
        // remove as Access-Control-Allow-Credentials is removed at Cors filter class
        // credentials: 'include',
        headers: {
            'Content-Type': 'application/json',
            'Authorization': credentials
        }
    })
    .then(res => {
        if (res.ok) {
            res.json().then(json => {
                localStorage.setItem("token", json.token);
                window.location.replace('/natter.html');
            });
        }
    })
    .catch(error => console.error('Error logging in: ', error));
}

window.addEventListener('load', function(e) {
    document.getElementById('login')
        .addEventListener('submit', processLoginSubmit);
});

function processLoginSubmit(e) {
    e.preventDefault();
    
    let username = document.getElementById('username').value;
    let password = document.getElementById('password').value;
    
    login(username, password);
    return false;
}

function createSpace(name, owner) {
    let data = {name: name, owner: owner};
    let token = localStorage.getItem("token");
    
    fetch(apiUrl + '/spaces', {
        method: 'POST',
        // remove so broswer won't send cookies
        // credentials: 'include',
        body: JSON.stringify(data),
        headers: {
            'Content-Type': 'application/json',
            // remove because we are not using cookies anymore
            // 'X-CSRF-Token': csrfToken
            'Authorization': 'Bearer ' + token
        }
    })
    .then(response => {
        if (response.ok) {
            return response.json();
        } else if (response.status === 401) {
            window.location.replace('/login.html');
        } else {
            throw Error(response.statusText);
        }
    })
    .then(json => console.log('Created space: ', json.name, json.uri))
    .catch(error => console.error('Error: ', error));
}
