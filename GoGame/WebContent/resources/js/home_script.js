/**
 * 
 */

function displaySectionsByLoginStatus(){
	if(document.getElementById('isLoggedIn').value == 'true'){
		document.getElementById('playerStatsSection').style.display='';
		document.getElementById('loginSection').style.display='none';
		document.getElementById('playButton').disabled = false;
	} else {
		document.getElementById('playerStatsSection').style.display='none';
		document.getElementById('loginSection').style.display='';
		document.getElementById('playButton').disabled = true;
	}
}

function onResponse(data){
	if(data.status === 'success'){
		displaySectionsByLoginStatus();
	}
}

displaySectionsByLoginStatus();