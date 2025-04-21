const myBoardDiv = document.getElementById('my-board');
const enemyBoardDiv = document.getElementById('enemy-board');
const messageArea = document.getElementById('message-area');
const startGameBtn = document.getElementById('start-game-btn');
const disconnectBtn = document.getElementById('disconnect-btn');
const clearShipsBtn = document.getElementById('clear-ships-btn');
const playerName = document.getElementById('player-name');

const boardSize = 10;
let socket;
let myBoardState = Array(boardSize).fill(null).map(() => Array(boardSize).fill('EMPTY'));
let enemyBoardState = Array(boardSize).fill(null).map(() => Array(boardSize).fill('CLOSED'));
let placingShips = true;


const IMAGE_SIZE = 30;
const ROWS = boardSize;
const COLUMNS = boardSize;
const IMAGES_PATH = '/icons/';
let selectedShipSize = 1;
let selectedOrientation = 'vertical';
const shipsLeft = { 1: 4, 2: 3, 3: 2, 4: 1 };

function createCellImage(pictureName, dataX = null, dataY = null) {
    const img = document.createElement('img');
    img.src = `${IMAGES_PATH}${pictureName}.png`;
    img.width = IMAGE_SIZE;
    img.height = IMAGE_SIZE;
    img.style.position = 'absolute';
    if (dataX !== null) img.dataset.x = dataX;
    if (dataY !== null) img.dataset.y = dataY;
    return img;
}

function loadEmptyBoard(boardDiv, isEnemy = false) {
    boardDiv.innerHTML = '';
    boardDiv.style.position = 'relative';
    boardDiv.style.width = `${COLUMNS * IMAGE_SIZE}px`;
    boardDiv.style.height = `${ROWS * IMAGE_SIZE}px`;
    const boardArray = Array(ROWS).fill(null).map(() => Array(COLUMNS).fill(isEnemy ? 'CLOSED' : 'EMPTY'));

    for (let i = 0; i < ROWS; i++) {
        for (let j = 0; j < COLUMNS; j++) {
            let pictureName = 'EMPTY';
            let xPos = IMAGE_SIZE * j;
            let yPos = IMAGE_SIZE * i;
            let dataX = j;
            let dataY = i;

            if (i === 0 && j === 0) continue;
            else if (i === 0 && j !== 0) pictureName = `SYM${j}`;
            else if (i !== 0 && j === 0) pictureName = `NUM${i}`;
            else pictureName = isEnemy ? 'CLOSED' : 'EMPTY';

            const cellImage = createCellImage(pictureName, dataX - (i > 0 && j > 0 ? 1 : 0), dataY - (i > 0 && j > 0 ? 1 : 0));
            cellImage.style.left = `${xPos}px`;
            cellImage.style.top = `${yPos}px`;
            if (i > 0 && j > 0) {
                cellImage.classList.add(isEnemy ? 'enemy-cell' : 'my-cell');
            }
            boardDiv.appendChild(cellImage);
        }
    }
    return boardArray;
}

function updateShipCounts() {
    document.getElementById('ships-left-1').innerText = shipsLeft[1];
    document.getElementById('ships-left-2').innerText = shipsLeft[2];
    document.getElementById('ships-left-3').innerText = shipsLeft[3];
    document.getElementById('ships-left-4').innerText = shipsLeft[4];
}

function handleMyBoardClick(event) {
    if (placingShips && event.target.tagName === 'IMG' && event.target.classList.contains('my-cell')) {
        const clickedX = parseInt(event.target.dataset.x);
        const clickedY = parseInt(event.target.dataset.y);

        for (let i = 0; i < boardSize; i++) {
            for (let j = 0; j < boardSize; j++) {
                let canPlace = false;

                if (selectedOrientation === 'vertical') {
                    if (j === clickedX && i <= clickedY && i + selectedShipSize > clickedY && canPlaceShip(j, i, selectedShipSize, selectedOrientation)) {
                        canPlace = true;
                    }
                } else {
                    if (i === clickedY && j <= clickedX && j + selectedShipSize > clickedX && canPlaceShip(j, i, selectedShipSize, selectedOrientation)) {
                        canPlace = true;
                    }
                }

                if (canPlace) {
                    placeShip(j, i, selectedShipSize, selectedOrientation);
                    return;
                }
            }
        }
    }
}

function handleEnemyBoardClick(event) {
    if (!placingShips && socket && socket.readyState === WebSocket.OPEN && event.target.classList.contains('enemy-cell')) {
        const x = parseInt(event.target.dataset.x);
        const y = parseInt(event.target.dataset.y);
        const message = JSON.stringify({ type: 'SHOT', payload: x, additionalPayload: y });
        socket.send(message);
        // Временно блокируем клики после выстрела до получения ответа
        enemyBoardDiv.removeEventListener('click', handleEnemyBoardClick);
        messageArea.innerText = 'Выстрел произведен. Ожидание результата...';
    }
}

function canPlaceShip(startX, startY, size, orientation) {
    if (shipsLeft[size] <= 0) return false;


    if (orientation === 'vertical') {
        if (startY < 0 || startY + size > boardSize) return false;
    } else {
        if (startX < 0 || startX + size > boardSize) return false;
    }

    for (let i = 0; i < size; i++) {
        let x = startX, y = startY;
        if (orientation === 'vertical') y += i;
        else x += i;
        if (myBoardState[y][x] === 'SHIP') {
            return false;
        }

        for (let row = y - 1; row <= y + 1; row++) {
            for (let col = x - 1; col <= x + 1; col++) {
                if (row >= 0 && row < boardSize && col >= 0 && col < boardSize && myBoardState[row][col] === 'SHIP') {
                    return false;
                }
            }
        }
    }
    return true;
}

function placeShip(startX, startY, size, orientation) {
    for (let i = 0; i < size; i++) {
        let x = startX, y = startY;
        if (orientation === 'vertical') y += i;
        else x += i;

        const imgElements = myBoardDiv.querySelectorAll(`img.my-cell[data-x='${x}'][data-y='${y}']`);
        if (imgElements.length > 0) {
            const imgElement = imgElements[0];
            imgElement.src = `${IMAGES_PATH}SHIP.png`;
            myBoardState[y][x] = 'SHIP';
        }
    }
    shipsLeft[selectedShipSize]--;
    updateShipCounts();
    if (Object.values(shipsLeft).every(count => count === 0)) {
        messageArea.innerText = 'Все корабли расставлены. Нажмите "Подтвердить расстановку".';
        startGameBtn.disabled = false;
        placingShips = false;
    }
}

function connectWebSocket() {
    socket = new WebSocket('/ws/battleship');

    socket.onopen = () => {
        messageArea.innerText = 'Подключено к серверу.';
        startGameBtn.disabled = false;
        disconnectBtn.disabled = false;
    };

    socket.onmessage = (event) => {
        const message = JSON.parse(event.data);
        console.log('Получено сообщение:', message);
        switch (message.type) {
            case 'FIELD':
                console.log('Получено поле противника:', message.payload);

                const receivedEnemyFieldData = message.payload;

                receivedEnemyFieldData.forEach(cellData => {
                    enemyBoardState[cellData.y][cellData.x] = cellData.isShip ? 'SHIP' : 'EMPTY';

                    const enemyCells = enemyBoardDiv.querySelectorAll(`.enemy-cell[data-x='${cellData.x}'][data-y='${cellData.y}']`);
                    if (enemyCells.length > 0) {
                        const imgElement = enemyCells[0];
                        imgElement.src = `${IMAGES_PATH}${cellData.isShip ? 'enemy_ship' : 'closed'}.png`;
                    }
                });
                messageArea.innerText = 'Поле противника получено.';
                break;
            case 'WAITING_FOR_OPPONENT':
                messageArea.innerText = 'Ожидание другого игрока...';
                break;
            case 'GAME_READY':
                messageArea.innerText = 'Игра готова. Расставьте корабли.';
                placingShips = true;
                startGameBtn.innerText = 'Подтвердить расстановку';
                startGameBtn.onclick = sendFieldToServer;
                break;
            case 'OPPONENT_FIELD':
                messageArea.innerText = 'Противник расставил корабли. Ваш ход.';
                placingShips = false;
                startGameBtn.disabled = true;
                startGameBtn.innerText = 'Начать игру';
                startGameBtn.onclick = null;

                enemyBoardState = loadEmptyBoard(enemyBoardDiv, true);
                enemyBoardDiv.addEventListener('click', handleEnemyBoardClick);
                break;
            case 'YOUR_TURN':
                messageArea.innerText = 'Ваш ход.';
                enemyBoardDiv.addEventListener('click', handleEnemyBoardClick);
                break;
            case 'OPPONENT_TURN':
                messageArea.innerText = 'Ход противника.';
                enemyBoardDiv.removeEventListener('click', handleEnemyBoardClick);
                break;
            case 'SHOT_RESULT':
                const hit = message.payload;
                const resultMessage = message.additionalPayload;
                const x = message.x;
                const y = message.y;
                const enemyCells = enemyBoardDiv.querySelectorAll(`img.enemy-cell[data-x='${x}'][data-y='${y}']`);
                if (enemyCells.length > 0) {
                    const enemyCellImg = enemyCells[0];
                    enemyCellImg.src = `${IMAGES_PATH}${hit ? 'destroy_ship' : 'point'}.png`;
                    enemyBoardState[y][x] = hit ? 'HIT' : 'MISS';
                }
                messageArea.innerText = resultMessage;
                if (message.yourTurn) {
                    messageArea.innerText = 'Ваш ход.';
                    enemyBoardDiv.addEventListener('click', handleEnemyBoardClick);
                } else {
                    messageArea.innerText = 'Ход противника.';
                    enemyBoardDiv.removeEventListener('click', handleEnemyBoardClick);
                }
                break;
            case 'OPPONENT_SHOT':
                const oppX = message.payload;
                const oppY = message.additionalPayload;
                const myCells = myBoardDiv.querySelectorAll(`img.my-cell[data-x='${oppX}'][data-y='${oppY}']`);
                if (myCells.length > 0) {
                    const myCellImg = myCells[0];
                    if (myBoardState[oppY][oppX] === 'SHIP') {
                        myCellImg.src = `${IMAGES_PATH}destroy_ship.png`;
                        myBoardState[oppY][oppX] = 'HIT';
                    } else {
                        myCellImg.src = `${IMAGES_PATH}point.png`;
                        myBoardState[oppY][oppX] = 'MISS';
                    }
                }
                break;
            case 'WINNER':
                messageArea.innerText = 'Вы победили!';
                enemyBoardDiv.removeEventListener('click', handleEnemyBoardClick);
                break;
            case 'DEFEAT':
                messageArea.innerText = 'Вы проиграли.';
                enemyBoardDiv.removeEventListener('click', handleEnemyBoardClick);
                break;
            case 'OPPONENT_DISCONNECTED':
                messageArea.innerText = 'Противник отключился. Вы победили!';
                enemyBoardDiv.removeEventListener('click', handleEnemyBoardClick);
                break;
            case 'ERROR':
                messageArea.innerText = 'Ошибка: ' + message.payload;
                break;
        }
    };

    socket.onclose = () => {
        messageArea.innerText = 'Соединение с сервером закрыто.';
        startGameBtn.disabled = true;
        disconnectBtn.disabled = true;
        enemyBoardDiv.removeEventListener('click', handleEnemyBoardClick);
    };

    socket.onerror = (error) => {
        messageArea.innerText = 'Ошибка соединения: ' + error;
        enemyBoardDiv.removeEventListener('click', handleEnemyBoardClick);
    };
}

function sendFieldToServer() {
    if (socket && socket.readyState === WebSocket.OPEN && placingShips === false) {

        if (Object.values(shipsLeft).every(count => count === 0)) {
            const fieldData = [];
            const ships = [];
            const placedShips = Array(boardSize).fill(null).map(() => Array(boardSize).fill(false));

            for (let y = 0; y < boardSize; y++) {
                for (let x = 0; x < boardSize; x++) {
                    const isShip = myBoardState[y][x] === 'SHIP';
                    fieldData.push({ x: x, y: y, isShip: isShip });
                }
            }

            for (let y = 0; y < boardSize; y++) {
                for (let x = 0; x < boardSize; x++) {
                    if (myBoardState[y][x] === 'SHIP' && !placedShips[y][x]) {
                        const newShip = { boxesOfShip: [] };
                        let currentX = x;
                        let currentY = y;
                        let isHorizontal = false;
                        let isVertical = false;

                        while (currentX < boardSize && myBoardState[y][currentX] === 'SHIP' && !placedShips[y][currentX]) {
                            newShip.boxesOfShip.push({ x: currentX, y: y });
                            placedShips[y][currentX] = true;
                            currentX++;
                            isHorizontal = true;
                        }

                        if (!isHorizontal) {
                            while (currentY < boardSize && myBoardState[currentY][x] === 'SHIP' && !placedShips[currentY][x]) {
                                newShip.boxesOfShip.push({ x: x, y: currentY });
                                placedShips[currentY][x] = true;
                                currentY++;
                                isVertical = true;
                            }
                        }

                        if (newShip.boxesOfShip.length > 0) {
                            ships.push(newShip);
                        }
                    }
                }
            }

            const message = JSON.stringify({ type: 'FIELD', payload: fieldData, additionalPayload: ships });
            socket.send(message);
            messageArea.innerText = 'Поле отправлено. Ожидание противника...';
            startGameBtn.disabled = true;
            startGameBtn.onclick = null;


            const enemyBoardContainer = document.querySelector('.enemy-board');
            if (enemyBoardContainer) {
                enemyBoardContainer.classList.remove('hidden');
            }

        } else {
            messageArea.innerText = 'Пожалуйста, расставьте все корабли!';
        }
    } else if (placingShips) {
        messageArea.innerText = 'Пожалуйста, завершите расстановку кораблей.';
    }
}

myBoardDiv.addEventListener('click', handleMyBoardClick);

document.querySelectorAll('input[name="ship-size"]').forEach(radio => {
    radio.addEventListener('change', () => {
        selectedShipSize = parseInt(radio.value);
    });
});

document.querySelectorAll('input[name="orientation"]').forEach(radio => {
    radio.addEventListener('change', () => {
        selectedOrientation = radio.value;
    });
});

startGameBtn.addEventListener('click', sendFieldToServer);
disconnectBtn.onclick = () => {
    if (socket && socket.readyState === WebSocket.OPEN) {
        socket.send(JSON.stringify({ type: 'DISCONNECT' }));
        socket.close();
    }
};
document.addEventListener("DOMContentLoaded", function () {
    const playerNameDisplay = document.getElementById('player-name');
    function getParameterByName(name, url = window.location.href) {
        const regex = new RegExp('[?&]' + name + '(=([^&#]*)|&|#|$)');
        const results = regex.exec(url);
        if (!results) return null;
        if (!results[2]) return '';
        return decodeURIComponent(results[2].replace(/\+/g, ' '));
    }
    const playerName = getParameterByName('playerName');
    if (playerNameDisplay && playerName) {
        playerNameDisplay.textContent = playerName + " поле";
    }
});
clearShipsBtn.addEventListener('click', () => {
    myBoardState = Array(boardSize).fill(null).map(() => Array(boardSize).fill('EMPTY'));
    loadEmptyBoard(myBoardDiv);
    shipsLeft[1] = 4;
    shipsLeft[2] = 3;
    shipsLeft[3] = 2;
    shipsLeft[4] = 1;
    updateShipCounts();
    placingShips = true;
});


let myBoard = loadEmptyBoard(myBoardDiv);
let enemyBoard = loadEmptyBoard(enemyBoardDiv, true);
connectWebSocket();