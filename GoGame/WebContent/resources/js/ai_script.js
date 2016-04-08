var aiMoveTimer;
var aiMoveHistory;

function aiRoutine(){
	aiMoveHistory = [];
    for(var row = 0; row < grids; row++){
    	aiMoveHistory[row] = [];
        for(var col = 0; col < grids; col++){
        	aiMoveHistory[row][col] = false;
        }
    }
	aiMove();
}

function aiMove(){
	
	if(player.isMyTurn){
		// simulate move
		if(aiMoveTimer){clearTimeout(aiMoveTimer);}
		var destCandidates = [];
	    for(var a = 0; a < grids; a++){
	        for(var b = 0; b < grids; b++){
	        	if(position[a][b].occupant === nobody_here && !aiMoveHistory[a][b]){
	        		destCandidates.push({
		        		x: position[a][b].x,
		        		y: position[a][b].y
		        	});
	        	}
	        }
	    }
	    
	    var randomIndex = Math.floor(Math.random() * destCandidates.length);
	    
	    var destX = destCandidates[randomIndex].x;
		var destY = destCandidates[randomIndex].y;
		
		var currX = canvasWidth;
		var currY = canvasHeight * Math.random();
		
		var thrust = 30;
		var dx = destX - currX, 
			dy = destY - currY,
			dist = Math.sqrt(dx*dx + dy*dy),
			velX = (dx / dist) * thrust,
			velY = (dy / dist) * thrust;
		
		
		function simulateMove(){
			if(currX == destX && currY == destY){
				onMouseClickOnBoard({offsetX: destX, offsetY: destY});
				for(var a = 0; a < grids; a++){
			        for(var b = 0; b < grids; b++){
			        	if(destY != position[a][b].y){break;}
			        	if(destX == position[a][b].x){
			        		aiMoveHistory[a][b] = true;
			        		aiMoveTimer = setTimeout(aiMove, 1000);
							return;
			        	}
			        }
			    }
			}
			if(Math.abs(destX - currX) <= Math.abs(velX)){
				currX = destX;
				velX = 0;
			}
			if(Math.abs(destY - currY) <= Math.abs(velY)){
				currY = destY;
				velY = 0;
			}
			currX += velX;
			currY += velY;
			onMouseMoveOverBoard({offsetX: currX, offsetY: currY});
			window.requestAnimationFrame(simulateMove);
		}
		simulateMove();
		
	} else {
		aiMoveTimer = setTimeout(aiMove, 1000);
	}
}

function aiRoutineEnd(){
	if(aiMoveTimer){clearTimeout(aiMoveTimer);}
}
