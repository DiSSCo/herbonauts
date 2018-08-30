*{~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
  
	Onglet Timeline
    
  ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~}* 

<div id="timeline" style="width:100%;height: 300px"></div>
    
<script type="text/javascript">

	var tl = null;
	
	var default_fillInfo = Timeline.DefaultEventSource.Event.prototype.fillInfoBubble;
	Timeline.DefaultEventSource.Event.prototype.fillInfoBubble = function (element, theme, labeller) {

		$.ajax({
			url: this.getLink(),
			success: function(html) {
				$(element).html(html);
			}
		});
		
		
	};
	
	function initializeTimeline() {
		if (tl != null) {
			return;
		}
	
		SimileAjax.History.enabled = false;
	
		var eventSource = new Timeline.DefaultEventSource();

		var bandInfos = [
			Timeline.createBandInfo({
				width:          '20%', 
				intervalUnit:   Timeline.DateTime.MONTH, 
				intervalPixels: 100,
				eventSource: eventSource
			}),
			Timeline.createBandInfo({
				width:          '60%', 
				intervalUnit:   Timeline.DateTime.YEAR, 
				intervalPixels: 200,
				eventSource: eventSource
			}),
			Timeline.createBandInfo({
				width:          '20%', 
				intervalUnit:   Timeline.DateTime.DECADE, 
				intervalPixels: 100,
				eventSource: eventSource
			})
		];

		bandInfos[1].syncWith = 0;
		bandInfos[2].syncWith = 0;
		
		tl = Timeline.create(document.getElementById('timeline'), bandInfos, Timeline.HORIZONTAL);
  
		tl.loadJSON('${_url}', 
				function(json, url) {
					eventSource.loadJSON(json, url);
				});
	  
		$.getJSON('${_urlMin}', function(date) {
			if (date) {
				tl.getBand(0).setCenterVisibleDate(Timeline.DateTime.parseGregorianDateTime(date))
			}
			
		})
	
	}
 
	// Initialisation de la timeline lors de l'affichage de l'onglet
	$('#${_tabId}').on('shown', function() {
		initializeTimeline();
	});
</script>
    