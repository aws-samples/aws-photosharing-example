var syncs = new Array();

startSync = function(id, callback, start) {	
	if (syncs[id] != null) {		
		syncs[id]['counter'] = syncs[id]['counter']+1;		
		console.log(id+":"+syncs[id]['counter']+1)
	} else {		
		var start_counter = eval(start);		
		syncs[id] = new Array();
		syncs[id]['counter'] = start==null?1:start_counter;
		syncs[id]['callback'] = callback;
		console.log(id+":"+syncs[id]['counter']);
	}
}

completeSync = function(id) {	
	if (syncs[id] != null) {				
		syncs[id]['counter'] = syncs[id]['counter']-1;
		console.log(id+":"+syncs[id]['counter']);
		if (syncs[id]['counter'] == 0) {			
			eval(syncs[id]['callback']);
			syncs[id] = null;
		}
	}
}