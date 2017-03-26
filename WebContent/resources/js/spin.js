
$(
	function() {
		//console.log($("#background-shares").children().length);
		var total_images = $("#background-shares").length;
		if (total_images > 0) {
			var index = 0;
			//console.log(index);
			//console.log($('#background-shares').children().eq(index).attr('data-source'));
			$('.content').css('background-image', 'url('+$('#background-shares').children().eq(index).attr('data-source')+')');
			$('#fp_media_name').text($('#background-shares').children().eq(index).attr('data-name'));
		}
	}		
);