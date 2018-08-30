var noHTML5 = false;

if (($.browser.msie && parseInt($.browser.version, 10) <= 9))
{
	noHTML5 = true;
}
else if ($.browser.mozilla && parseInt($.browser.version, 10) <= 2)
{
	noHTML5 = true;
}

if (noHTML5)
{
	$(document).ready(function() {
		$("input[type='text']").each(function(index, input) {
			var placeHolder = $(input).attr("placeholder");
			
			if ($(input).val() == "") {
				$(input).attr("value", placeHolder);
			}
			
			$(input).focusin(function(event) {
				if ($(input).val() == placeHolder)
				{
					$(input).val("");
				}
			});
		
			$(input).focusout(function(event) {
				if ($(input).val() == "")
				{
					$(input).val(placeHolder);
				}
			});
		});
	});
	
	$(document).ready(function() {
		$("input[type='password']").each(function(index, input) {
			var placeHolderValue = $(input).attr("placeholder");
			var placeHolderTag = $("<input type='text' value='" + placeHolderValue + "' />");
			$(input).after(placeHolderTag);
			
			if ($(input).val() == "") {
				$(input).hide();
				placeHolderTag.show();
			}
			else {
				$(input).show();
				$(input).focus();
				placeHolderTag.hide();
			}
			
			placeHolderTag.focus(function(event) {	
					$(input).show();
					$(input).focus();
					placeHolderTag.hide();
			});
		
			$(input).focusout(function(event) {
				if ($(input).val() == "")
				{
					$(input).hide();
					placeHolderTag.show();
				}
				
			});
		});
	});
}