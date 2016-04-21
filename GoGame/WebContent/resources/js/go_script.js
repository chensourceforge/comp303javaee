/**
 * temporary variables
 */
var row, col, i;

/**
 * message constants - action:
 */
var standby = 1,
    find_opponent = 2,
    opponent_found = 3,
    begin_game = 4,
    your_turn = 5,
    opponent_turn = 6,
    my_move = 7,
    opponent_move = 8,
    my_click = 9,
    invalid_click = 10,
    set_your_stone = 11,
    set_opponent_stone = 12,
    my_stats = 13,
    you_win = 14,
    opponent_win = 15,
    opponent_disconnect = 16,
    rematch_request = 17,
    rematch_wait_opponent = 18,
    rematch_ready = 19,
    rematch_opponent_requested = 20;

/**
 * player
 */
var player = {};

/**
 * canvas
 */
var canvas_id = "board",
    canvasWidth, 
    canvasHeight,
    canvas, context;



/**
 * game board
 */
var grids,
    gridSpace, 
    stoneRadius, 
    boardColor = "#DCB35C";


/**
 * occupancy constants
 */
var nobody_here = 0, black_here = 1, white_here = 2;


/**
 * board representation
 *   position[row][col].occupant
 *   position[row][col].x
 *   position[row][col].y
 */
var position;

/**
 * websocket
 */
var socket;

/**
 * rules
 */
var isDisplayRules = false;


document.addEventListener("DOMContentLoaded", function(event) {
	// paint 9x9 grids by default
	document.getElementById("selGrids9").checked = true;
	onBoardSpecsChange(9);
	
	player.name = document.getElementById("playerName").innerHTML;
});

function initVariables(){
    // player
	player.isBlack = false;
	player.isMyTurn = false;
	player.opponentName = "";
	player.points = 0;
	player.opponentPoints = 0;
    
    // board representation
    position = [];
    for(row = 0; row < grids; row++){
        position[row] = [];
        for(col = 0; col < grids; col++){
            position[row][col] = {
                    occupant: nobody_here//,
                    //x: gridSpace * col + gridSpace,
                    //y: gridSpace * row + gridSpace
            };
        }
    } 
} // END - initVariables()

function setBoardOrientation(){
	if(player.isBlack){
		// REGULAR
	    for(row = 0; row < grids; row++){
	        for(col = 0; col < grids; col++){
	        	position[row][col].x = gridSpace * col + gridSpace;
	        	position[row][col].y = gridSpace * row + gridSpace;
	        }
	    }
	} else {
		// FLIPPED
		for(row = 0; row < grids; row++){
	        for(col = 0; col < grids; col++){
	        	position[row][col].x = gridSpace * (grids - col);
	        	position[row][col].y = gridSpace * (grids - row);
	        }
	    }
	}
}

function reInitVariables(){
	// player
	player.isMyTurn = false;
	player.points = 0;
	player.opponentPoints = 0;
    
    // board representation
    for(row = 0; row < grids; row++){
        for(col = 0; col < grids; col++){
            position[row][col].occupant = nobody_here;
        }
    } 
}

// returns int
function getRow(y){
	var result;
	if((y % gridSpace) < (gridSpace / 2)){
		result = Math.floor(y / gridSpace) - 1;
	} else {
		result = Math.floor(y / gridSpace);
	}
	return result < 0 ? 0 : (result >= grids ? grids-1 : result);
}
function getColumn(x){
	var result;
	if((x % gridSpace) < (gridSpace / 2)){
		result = Math.floor(x / gridSpace) - 1;
	} else{
		result = Math.floor(x / gridSpace);
	}
	return result < 0 ? 0 : (result >= grids ? grids-1 : result);
}
// sets position array
function setPlayerStone(col, row){
    if(player.isBlack){
        position[row][col].occupant = black_here;
    } else {
        position[row][col].occupant = white_here;
    }
}
function setOpponentStone(col, row){
    if(player.isBlack){
        position[row][col].occupant = white_here;
    } else {
        position[row][col].occupant = black_here;
    }
}
function removeStone(col, row){
    position[row][col].occupant = nobody_here;
}
function removeStones(removeArray){
    // [col,row, col,row, ...]
    var len = removeArray.length;
    if(len % 2 === 0){
        for(i=0; i<len; i+=2){
            col = removeArray[i];
            row = removeArray[i+1];
            removeStone(col, row);
        }
    }
}

/**
 * paints empty board with grid lines
 */
function paintBoard(){
    
	// paint bg color
	context.clearRect(0, 0, canvasWidth, canvasHeight);
	context.fillStyle = boardColor;
	context.fillRect(0, 0, canvasWidth, canvasHeight);
	
	// draw border
	var borderWidth = 10;
	var borderOffset = borderWidth / 2;
    context.lineWidth = borderWidth;
	
	context.beginPath();
	context.moveTo(borderOffset, borderOffset);
	context.lineTo(canvasWidth - borderOffset, borderOffset);
	
	context.lineTo(canvasWidth - borderOffset, canvasHeight - borderOffset);
	
	context.lineTo(borderOffset, canvasHeight - borderOffset);
	
	context.closePath();
	context.stroke();
	
	// draw grids
	var gridLineWidth = 2;
	context.lineWidth = gridLineWidth;
	for(row = 1; row <= grids; row++){
		context.moveTo(gridSpace, gridSpace * row);
		context.lineTo(canvasWidth - gridSpace, gridSpace * row);
		context.moveTo(gridSpace * row, gridSpace);
		context.lineTo(gridSpace * row, canvasHeight - gridSpace);
	}
	context.stroke();
} // END - paintBoard()

/**
 * draws board along with stones
 */
function rePaintBoard(){
    paintBoard();
    
    // draw existing stones
    for(row = 0; row < grids; row++){
		for(col = 0; col < grids; col++){
			switch(position[row][col].occupant){
				case black_here:// paint black stone
					drawBlackStone(position[row][col].x, position[row][col].y);
					break;
				case white_here:// paint white stone
					drawWhiteStone(position[row][col].x, position[row][col].y);
					break;
                default:
                    break;
			}
		}
	}
}

function drawBlackStone(x, y){
	var grdBlack = context.createRadialGradient(5+x,10+y,30,0+x,4+y,1);
	grdBlack.addColorStop(0,"black");
	grdBlack.addColorStop(1,"gray");
	context.fillStyle = grdBlack;
	context.beginPath();
	context.arc(x, y, stoneRadius, 0, 2*Math.PI);
	context.fill();
}
function drawWhiteStone(x, y){
	var grdWhite = context.createRadialGradient(5+x,5+y,3,0+x,5+y,90);
	grdWhite.addColorStop(0,"white");
	grdWhite.addColorStop(1,"black");
	context.fillStyle = grdWhite;
	context.beginPath();
	context.arc(x, y, stoneRadius, 0, 2*Math.PI);
	context.fill();
}
function drawPlayerStone(x, y){
	if(player.isBlack){drawBlackStone(x, y);}
	else{drawWhiteStone(x, y);}
}
function drawOpponentStone(x, y){
	if(!player.isBlack){drawBlackStone(x, y);}
	else{drawWhiteStone(x, y);}
}

function drawText(str){
    context.font="50px Comic Sans MS";
    context.textAlign = "center";
    context.fillText(str, canvasWidth / 2, canvasHeight / 2);
}
function drawBlueText(str){
	context.fillStyle = "blue";
	drawText(str);
}
function drawGreenText(str){
	context.fillStyle = "green";
	drawText(str);
}
function drawRedText(str){
	context.fillStyle = "red";
	drawText(str);
}

function updatePlayersNames(){
	if(player.isBlack){
    	document.getElementById("txtBlackName").innerHTML = player.name;
    	document.getElementById("txtWhiteName").innerHTML = player.opponentName;
    	
    } else {
    	document.getElementById("txtBlackName").innerHTML = player.opponentName;
    	document.getElementById("txtWhiteName").innerHTML = player.name;
    }
}
function updatePointsFields(){
	if(player.isBlack){
    	document.getElementById("txtBlackPoints").innerHTML = player.points;
        document.getElementById("txtWhitePoints").innerHTML = player.opponentPoints;
    } else {
    	document.getElementById("txtBlackPoints").innerHTML = player.opponentPoints;
        document.getElementById("txtWhitePoints").innerHTML = player.points;
    }
}

function setPlayerTurn(isMyTurn){
	if(isMyTurn){
		player.isMyTurn = true;
		canvas.addEventListener("mousemove", onMouseMoveOverBoard);
		canvas.addEventListener("click", onMouseClickOnBoard);
	} else {
		player.isMyTurn = false;
		canvas.removeEventListener("mousemove", onMouseMoveOverBoard);
		canvas.removeEventListener("click", onMouseClickOnBoard);
	}
}

function stopGame(){
	if(socket){socket.close();}
    setPlayerTurn(false);
    paintBoard();
    document.getElementById("pointsArea").style.display = "none";
    document.getElementById("btnStartStop").value = "Start Game";
    document.getElementById("selGrids9").disabled = false;
	document.getElementById("selGrids13").disabled = false;
	document.getElementById("selGrids19").disabled = false;
}
function startGame(){
	document.getElementById("btnStartStop").value = "Quit Game";
	document.getElementById("selGrids9").disabled = true;
	document.getElementById("selGrids13").disabled = true;
	document.getElementById("selGrids19").disabled = true;		
    initVariables();
    startWebsocketConnection();
}
function reStartGame(){
	document.getElementById("btnStartStop").value = "Quit Game";
	setPlayerTurn(false);
    reInitVariables();
    paintBoard();
    socket.send(JSON.stringify({action: rematch_request}));
}

function toggleDisplayRules(){
	isDisplayRules = !isDisplayRules;
	if(isDisplayRules){document.getElementById("txtRules").style.display = "block";} 
	else {document.getElementById("txtRules").style.display = "none";}
}

function onHoverName(elem){
	document.getElementById("txtOpponentName").value = elem.innerHTML;
	document.getElementById("btnGetOppInfo").click();
}

function onOpponentInfoReceived(data){
	if(data.status === 'success'){
		document.getElementById("secOpponentInfo").style.display = "";
		setTimeout(function(){
			document.getElementById("secOpponentInfo").style.display = "none";
		}, 2000);
	}
}

function onBoardSpecsChange(numGrids){
    // canvas
    canvasWidth = 500;
    canvasHeight = canvasWidth;
    canvas = document.getElementById(canvas_id);
    canvas.width = canvasWidth;
	canvas.height = canvasHeight;
    
    // game board
    grids = numGrids; /* change this when user clicks radio button */
    gridSpace = canvasWidth / (grids + 1);
    stoneRadius = gridSpace * 40 / 100;
    
    // get context from canvas 
    context = canvas.getContext("2d");
    paintBoard();
}


function onStartStopGameClick(){
	var command = document.getElementById("btnStartStop").value;
	switch(command){
		case "Start Game":
			startGame();
			break;
		case "Quit Game":
			stopGame();
			// test for ai username --> tester1, tester2, etc.
			if(player.name.match(/^tester[0-9]+$/)){aiRoutineEnd();}
			break;
		case "Rematch":
			reStartGame();
			break;
	}
}

function onMouseMoveOverBoard(event){
	if(player.isMyTurn){
		var x = event.offsetX;
		var y = event.offsetY;
        
        socket.send(JSON.stringify({
            action: my_move,
            x: player.isBlack ? x : canvasWidth - x, // send REGULAR pos to server
            y: player.isBlack ? y : canvasHeight - y // send REGULAR pos to server
        }));
		
		rePaintBoard(); 
		drawPlayerStone(x, y);
	}
}

function onMouseClickOnBoard(event){
	if(player.isMyTurn){
        
        setPlayerTurn(false);
        
        if(player.isBlack){
        	row = getRow(event.offsetY);
            col = getColumn(event.offsetX);
        } else {
        	row = getRow(canvasHeight - event.offsetY);
            col = getColumn(canvasWidth - event.offsetX);
        }
        
//        row = getRow(event.offsetY);
//        col = getColumn(event.offsetX);
        
        socket.send(JSON.stringify({
            action: my_click,
            row: row,
            col: col
        }));
	}
}

function startWebsocketConnection(){
    socket = new WebSocket("ws://localhost:8088/GoGame/game");
    
	socket.onopen = function(){
        socket.send(JSON.stringify({
            action: my_stats,
            grids: grids,
            name: player.name
        }));
		console.log("client: open");
	};

	socket.onclose = function(){
		console.log("client: close");
	};
    
    socket.onmessage = function(event){
        var jsonObj = JSON.parse(event.data);
        switch(jsonObj.action){
            case standby:
            	paintBoard();
                drawBlueText("Standby..");
                socket.send(JSON.stringify({action: find_opponent}));
                break;
            case opponent_found:
            	paintBoard();
                drawBlueText("Opponent found!");
                player.opponentName = jsonObj.opponentName;
                player.isBlack = jsonObj.isBlack;
                
                setBoardOrientation();
                updatePlayersNames();
                updatePointsFields();                
                document.getElementById("pointsArea").style.display = "block";
                
                // test for ai username --> tester1, tester2, etc.
    			if(player.name.match(/^tester[0-9]+$/)){aiRoutine();}
                
                socket.send(JSON.stringify({action: begin_game}));
                break;
            
            case your_turn:
            	rePaintBoard();
                drawGreenText("Your turn");
                setPlayerTurn(true);
                break;
            case opponent_turn:
            	rePaintBoard();
                drawBlueText("Opponent's turn");
                setPlayerTurn(false);
                break;
            case opponent_move:
                rePaintBoard();
                if(player.isBlack){
                	drawOpponentStone(jsonObj.x, jsonObj.y);
                } else {
                	drawOpponentStone(canvasWidth - jsonObj.x, canvasHeight - jsonObj.y);
                }
                //drawOpponentStone(jsonObj.x, jsonObj.y);
                break;
            case invalid_click:
                drawRedText("invalid move");
                setPlayerTurn(true);
                break;
            case set_your_stone:
                setPlayerStone(jsonObj.col, jsonObj.row);
                if(jsonObj.remove){removeStones(jsonObj.remove);}
                player.points = jsonObj.yourPoints;
                player.opponentPoints = jsonObj.opponentPoints;
                rePaintBoard();
                updatePointsFields();
                break;
            case set_opponent_stone:
                setOpponentStone(jsonObj.col, jsonObj.row);
                if(jsonObj.remove){removeStones(jsonObj.remove);}
                player.points = jsonObj.yourPoints;
                player.opponentPoints = jsonObj.opponentPoints;
                rePaintBoard();
                updatePointsFields();
                break;
            
            case you_win:
                drawGreenText("You WON!");
                document.getElementById("btnStartStop").value = "Rematch";
                // end game
                // re-init Play button?
                break;
            case opponent_win:
                drawRedText(player.opponentName + " WON!");
                document.getElementById("btnStartStop").value = "Rematch";
                // end game
                // re-init Play button?
                break;
            case opponent_disconnect:
                alert(player.opponentName + " has disconnected.");
                stopGame();
                startGame();
                break;
            case rematch_wait_opponent:
	            paintBoard();
	            drawBlueText("Waiting for " + player.opponentName);
                break;
            case rematch_opponent_requested:
            	paintBoard();
	            drawBlueText(player.opponentName + " wants rematch");
            	break;
            case rematch_ready:
                player.isBlack = jsonObj.isBlack;
                setBoardOrientation();
                updatePlayersNames();
                updatePointsFields();
                
                // test for ai username --> tester1, tester2, etc.
    			if(player.name.match(/^tester[0-9]+$/)){aiRoutine();}
    			
                socket.send(JSON.stringify({action: begin_game}));
            	break;
            default:
                console.log("unknow action: " + jsonObj.action);
                break;
        }
    };
}

