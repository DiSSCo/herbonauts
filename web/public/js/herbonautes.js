
// Module Herbonautes
var herbonautes = {
		templates: {},
		actions: {},
		needReload: false
};

$.ajaxSetup({ cache: false });

herbonautes.successMessage = function(message) {
	$('#alert-success-content').text(message);
	$('#alert-success-box').show();
}

herbonautes.templateForActivity = function(activity) {
	if (!herbonautes.templates.activities || 
			!herbonautes.templates.activities[activity.type]) {
		return null;
	}
	return herbonautes.templates.activities[activity.type];
}

herbonautes.renderActivity = function(activity) {
	var template = herbonautes.templateForActivity(activity);
	if (!template) {
		return null;
	}

    //console.log(activity, template);
	
	// i18nize for mustache
	activity.i18n = {
		contribution: {
			type: function() {
				return i18n('contribution.type.' + activity.contribution.type.toLowerCase());
			}
		},
		badge: {
			type: function() {
				return i18n('badge.' + activity.badge.type.toLowerCase());
			}
		}
	};
	
	var _links = {
			user: function(user) {
				console.log("link user", user);
				if (user.deleted) {
					return '<i>' + i18n('user.deleted.account') + '</i>';
				} else {
					return '<a href="' + herbonautes.actions.toUser({login: user.login}) + '">'
						+ user.login + '</a>';
				}
			},
			mission: function(mission, tab) {
				return '<a href="' + herbonautes.actions.toMission({id: mission.id}) + (tab ? '?tab=' + tab : '') + '">'
				+ mission.title + '</a>';
			},
			specimen: function(specimen) {
				var linkTemplate = '<a href="{{url}}">{{specimen.genusSpecies}} ({{specimen.institute}}/{{specimen.collection}}/{{specimen.code}})</a>';
				return Mustache.render(linkTemplate, {
					url: herbonautes.actions.toSpecimen({
							institute: specimen.institute,
							collection: specimen.collection,
							code: specimen.code
						}),
					specimen: specimen
				})
			}
	};
	
	// links
	activity.link = {
		user: function () {
			return _links.user(activity.user);
		},
		mission: function () {
			return _links.mission(activity.mission);
		},
        specimen: function () {
            return _links.specimen(activity.specimen);
        },
		comment: {
			mission: function () {
				return _links.mission(activity.mission, 'comments');
			},
			user: function() {
				return _links.user(activity.user, 'comments');
			},
			specimen: function() {
				return _links.specimen(activity.specimen, 'comments');
			}
		},
		contribution: {
			mission: function () {
				return _links.mission(activity.contribution.mission);
			},
			specimen: function() {
				return _links.specimen(activity.contribution.specimen);
			}
		}
	}
	
	return Mustache.render(template, activity)
}

herbonautes.activityImageURL = function (activity) {
	if (activity.user) {
		return userImageAction({ login: activity.user.login, imageId: activity.user.imageId || 'nopic' });
	} else if (activity.mission) {
		return missionImageAction({ id: activity.mission.id, imageId: activity.mission.imageId });
	} else {
		return null;
	}
}

herbonautes.userImageURL =  function (login, imageId) {
	return userImageAction({ login: login, imageId: imageId });
}

herbonautes.initActivityList = function(listSelector, 
		moreButtonSelector, 
		loadingButtonSelector, 
		noDataElement) {
 	$(listSelector).bind('page-received', function(event, datas, page) {
  		
  		$(loadingButtonSelector).hide();
  		$(moreButtonSelector).show();
  		
  		$.each(datas, function(index, activity) {
  			
  			var avatar = $('<div/>')
  				.addClass('avatar')
  				.append($('<img />').attr('src', herbonautes.activityImageURL(activity)));
  			
  			var text = $('<div/>')
  				.addClass('text')
  				.html(herbonautes.renderActivity(activity));
  				
  			var since = $('<p/>')
  				.addClass('since')
  				.html(i18n('message.since', activity.date.since))
  				.appendTo(text);
  			
  			var clear = $('<div/>').addClass('clearfix');	
  				
  			$('<li/>')
  				.data('activity-id', activity.id)
  				.append(avatar)
  				.append(text)
  				.append(clear)
  				.appendTo(listSelector);
  		});

  		if (page > 1) {
	  		$(window).scrollTop(
	  				$(listSelector).height() + 
	  				$(listSelector).position().top - 
	  				$(window).height() + 
	  				100); // 100 px de marge
  		}
  		
  		return false;
  		
  	}).bind('page-loading', function(event) {
  		$(moreButtonSelector).hide();
  		$(loadingButtonSelector).show();
  	}).bind('page-error', function(event) {
  		$(moreButtonSelector).hide();
  		$(loadingButtonSelector).show();
  	}).bind('page-no-data', function(event) {
  		$(moreButtonSelector).hide();
  		$(loadingButtonSelector).hide();
  		$(noDataElement).show();
  	});
      	
}

herbonautes.htmlizeComment = function(comment) {
	var avatar = $('<div/>')
	.addClass('avatar')
	.append(
		$('<img />')
			.attr('src', herbonautes.userImageURL(comment.user.login, comment.user.imageId || 'nopic'))
	);

	var text = $('<div/>')
		.addClass('text');
		
		
	text.append(herbonautes.links.user(comment.user));
	
	text.append(' ');
	
	var since = $('<span/>')
		.addClass('since')
		.html(i18n('message.since', comment.date.since))
		.appendTo(text);
	
	text.append('<br/>');
	
	text.append($('<pre>').text(comment.text).html());
	
	var li = $('<li/>') 
		.data('comment-id', comment.id)
		.addClass('clearfix')
		.append(avatar)
		.append(text);
	
	return li;
}

herbonautes.htmlizeContribution = function(contribution) {

	//console.log("contribution ", contribution);

	var text = $('<div/>')
		.addClass('text');

	text.append(herbonautes.links.mission({ id: contribution.missionId, title: contribution.missionTitle}));
	text.append(' : ');
	text.append('<b>' + contribution.question.label + '</b>');
	text.append(' ' + i18n('for') + ' ');
	text.append(herbonautes.links.specimen(contribution.specimen));
	text.append('<br/>');

	var since = $('<span/>')
		.addClass('since')
		.html(i18n('message.since', contribution.since))
		.appendTo(text);

	//text.append(herbonautes.links.mission(contribution.mission));
	//text.append(' : ');
	//
	//text.append(i18n('contribution.type.' + contribution.type.toLowerCase()));
	//text.append(' ' + i18n('for') + ' ');
	//text.append(herbonautes.links.specimen(contribution.specimen));
	//text.append('<br/>');
	//
	//var since = $('<span/>')
	//.addClass('since')
	//.html(i18n('message.since', contribution.date.since))
	//.appendTo(text);
	
	var li = $('<li/>') 
		.addClass('clearfix')
		.append(text);
	
	return li;
}

herbonautes.appendComment = function(selector, comment, options) {
	if (options && options.top) {
		herbonautes.htmlizeComment(comment).prependTo(selector);
	} else {
		herbonautes.htmlizeComment(comment).appendTo(selector);
	}
	
}

herbonautes.markAlertAsRead = function(id) {
	function removeAlertElements() {
		$('li[data-alert-id="' + id + '"]').remove();
		if ($('.alert-box li').length == 0) {
			$('.alert-box').hide();
		} else {
			$('.alert-box li.hide').first().show();
		}
		
		
	}

	$.ajax({
		type: 'GET',
		url: herbonautes.actions.toMarkAlertAsRead({id: id}),
		success: removeAlertElements
	});
	
}

var lastEvent = null;

function initLogoutLink() {
	/*$('#logout-link').click(function() {
		herbonautes.logout();
		return false;
	});*/
}

function unknownErrorModal() {
}

function initAnnouncementsBox() {
	
	var $box = $('.announcements');
	if ($box.lenght == 0) {
		return;
	}
	$('.announcements .next').click(showNextAnnouncement);
	$('.announcements .prev').click(showPreviousAnnouncement);
	
}

function switchAnnouncements(cur, next) {
	cur.fadeOut(0);
	cur.addClass('hide');
	next.fadeIn(200);
	next.removeClass('hide');
}

function showNextAnnouncement() {
	var $cur = $('.announcement').not('.hide');
	var $next = $cur.next('.hide');
	if ($next.length == 0) {
		$next = $('.announcement').first();
	}
	switchAnnouncements($cur, $next);
	return false;
}

function showPreviousAnnouncement() {
	var $cur = $('.announcement').not('.hide');
	var $next = $cur.prev('.hide');
	if ($next.length == 0) {
		$next = $('.announcement').last();
	}
	switchAnnouncements($cur, $next);
	return false;
}


function activateLink(view) {
	$('.filter a').removeClass('active');
	$('.filter a[data-view="' + view + '"]').addClass('active');
}

function refresh(selector, onFinished) {
	var url = $(selector).data('refresh-url');
	if (url) {
		$.getJSON(url, function(json) {
			$(selector).text(json.count);
			if (json.loading) {
				setTimeout(refresh, 1000, selector, onFinished); 
			} else {
				if (onFinished) onFinished();
			}
		});
	}
}

function fillProfileMenu(user) {
	$.ajax({
		type: 'GET',
		url: herbonautes.actions.toProfileMenu(),
		success: function(menu) {
			$('#profile-menu').html($(menu).html());
			initLogoutLink();
			$('.login.nav-collapse.pull-right').hide();
			$('#profile-menu').show();
		},
		error: function() {
			//$('#login-modal #login-error').show();
		}
	});
}

function fillMissionsMenu(user) {
	$.ajax({
		type: 'GET',
		url: herbonautes.actions.toMissionsMenu(),
		success: function(menu) {
			$('#menu-missions').addClass('dropdown').html($(menu).html());
		},
		error: function() {
			//$('#login-modal #login-error').show();
		}
	});
}

function redirectIfVisitorIndex() {
	if (visitorIndex) {
		window.location.href = herbonautesBaseUrl;
		return true;
	}
	return false;
}

function currentLang() {
	return $('body').data('lang') || 'fr';
}

function refreshIfLanguageChange(user) {
	if (user.lang != currentLang()) {
		// refresh
		window.location.href = window.location.href;
	}
	// Au cas ou l'utilisateur ait une langue préférée différente
}

function onSignupError(jqXHR) {
	$('#signup-form input').removeClass('error');
	$('#signup-form .text-error').hide();
	if (jqXHR.status == 400) { // Droits OK mais requete invalide
		var errors = $.parseJSON(jqXHR.responseText);
		
		$.each(errors, function(key, errorArray) {
			$('#signup-form input[name="' + key + '"]').addClass('error');
			$('#signup-form .error-' + key).text(i18n(errorArray[0].message)).show();
		});
	} else {
		errorAlert();
	}
}

function onConnect() {
	redirectIfVisitorIndex();

	fillProfileMenu();
	fillMissionsMenu();
	showConnected();
	$('#login-modal').modal('hide');
}

function showConnected() {
	if (herbonautes.needReload) {
		window.location.reload();
	}
}


function loginProcess(success) {
	
	$('#login-form').submit(function() {
		var $form = $(this);
		var ok = false;
		$.ajax({
			type: 'POST',
			url: $form.attr('action'),
			data: $form.serialize(),
			success: function(user) {
				if (redirectIfVisitorIndex()) {
					return;
				}
				fillProfileMenu();
				showConnected();
				fillMissionsMenu();
				if (typeof onUserConnectedAfter === 'function') {
					onUserConnectedAfter()
				}
				$('#login-modal').modal('hide');
			},
			error: function() {
				$('#login-modal #login-error').show();
			}
		});
		return false;
	});
	
	$('#send-password-link').click(function() {
		$(this).hide();
		$('#password-form').show();
	});
	
	$('#password-form').submit(function() {
		var $form = $(this);
		var ok = false;
		$.ajax({
			type: 'POST',
			url: $form.attr('action'),
			data: $form.serialize(),
			success: function(user) {
				$('#login-modal').modal('hide');
				herbonautes.successMessage(i18n('alert.password.sent'));
			},
			error: function() {
				$('#login-modal #password-error').show();
			}
		});
		return false;
	});
	
	$('#signup-form').submit(function() {
		var $form = $(this);
		$.ajax({
			type: 'POST',
			url: $form.attr('action'),
			data: $form.serialize(),
			success:  function(user) {
				fillProfileMenu(user);
				fillMissionsMenu(user);
				showConnected();
				//alert('Vous etes maintenant inscrit'); // ICI ON MET A JOUR LE
														// MENU
				window.location.reload();
			},
			error: function(jqXHR, textStatus, errorThrown) {
				onSignupError(jqXHR);
			}
		});
		return false;
	});
	
	$('#login-modal').on('shown', function() {
		$('input[type="text"]', this).first().focus();
	})
	
	$('#login-modal').modal('show');
} 

$(function() {
	
	$('.filter a').click(function() {
		var view = $(this).data('view');
		var url = $(this).data('url');
		
		$('#activity-list').load(url);
		
		history.pushState({ view: view }, null, this.href)
		
		// $('#activity-list').append($('<li>').text('>>> going to ' + view));
		
		activateLink(view);
		
		return false;
	});
	
	$('.mark-alert-read').click(function() {
		herbonautes.markAlertAsRead($(this).data('alert-id'));
	});
	
	$(window).bind('popstate', function(event) {
		
		var DEFAULT_ACTIVITIES_VIEW = 'all';
		
		if (event.originalEvent.state != null) {
			var view = event.originalEvent.state.view;
			// $('#activity-list').append($('<li>').text('<<< going back to ' +
			// view));
			$('#activity-list').data('view', view);
			activateLink(view);
		} else {
			// $('#activity-list').append($('<li>').text('--- arriving'));
			history.replaceState({ view: DEFAULT_ACTIVITIES_VIEW }, null, location.href)
			activateLink(DEFAULT_ACTIVITIES_VIEW);
			$('#activity-list').data('view', DEFAULT_ACTIVITIES_VIEW);
		}
	});
	
	
	// ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
	// Dual butttons
	// ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
	$('.btn-dual .btn-over').hide();
	$('.btn-dual').each(function(i, elt) {
		var $btnOut = $(this).find('.btn-out');
		if ($btnOut.data('class')) $(this).addClass($btnOut.data('class'));
	});
	$('.btn-dual').mouseover(function() {
		var $btnOut = $(this).find('.btn-out');
		var $btnOver = $(this).find('.btn-over');
		if ($btnOut.data('class')) $(this).removeClass($btnOut.data('class'));
		if ($btnOver.data('class')) $(this).addClass($btnOver.data('class'));
		$btnOut.hide();
		$btnOver.show();
	}).mouseout(function() {
		var $btnOut = $(this).find('.btn-out');
		var $btnOver = $(this).find('.btn-over');
		if ($btnOver.data('class')) $(this).removeClass($btnOver.data('class'));
		if ($btnOut.data('class')) $(this).addClass($btnOut.data('class'));
		$btnOver.hide();
		$btnOut.show();
	});


	

	
	$.fn.openBox= function() { 
		var $box = $(this).filter('.contribution-box');
		$box.addClass('opened');
		$box.removeClass('closed');
		return $(this);
	}
	
	$.fn.closeBox = function() { 
		var $box = $(this).filter('.contribution-box');
		$box.removeClass('opened');
		$box.addClass('closed');
		return $(this);
	}
	
	// 
	// Affiche ou cache un ou plusieurs éléments lorsqu'un bouton radio
	// ou une checkbox est coché.
	//
	// Exemples :
	// <input type="checkbox" oncheck-hide="#toHideId" />
	// <input type="radio" oncheck-show=".toShowClass" />
	//
	function toggleOnCheck() {
		var $el = $(this);
		if ($el.is(':checked')) {
			$($el.data('oncheck-show')).show();
			$($el.data('oncheck-hide')).hide();
		} else {
			$($el.data('oncheck-show')).hide();
			$($el.data('oncheck-hide')).show();
		}
	}
	
	// toggleOnCheck on DOM ready
	/*
	 * $('input[type="radio"], input[type="checkbox"]').each(function (i, elt) {
	 * var $elt = $(elt); toggleOnCheck($elt); $elt.change(toggleOnCheck); });
	 */
	
	$('input[type="radio"], input[type="checkbox"]')
		.click(toggleOnCheck)
		.each(toggleOnCheck);
	
	
	
	// PAGINATION ------
	
	/**
	 * On positionne la class "paginable" sur le conteneur paginate-url contient
	 * l'url de mise à jour before-attr est l'attribut qui contiendra un
	 * identifiant permettant de rechercher les prochains éléments en fonction
	 * du dernier affiché (si la liste change souvent)
	 */
	$.fn.extend({
		paginate: function() {
			var $paginable = $(this);
			var url = $paginable.data('paginate-url');
			if (url == null) {
				return false;
			}
			
			$paginable.trigger('page-loading');
			
			var data;
			
			var nextPage = (($paginable.data('current-page') || 0) + 1);
			
			if ($paginable.data('before-attr')) {
				var beforeType = $paginable.data('before-attr');
				var top = ($paginable.data('insert-type') == 'prepend');
				
				//var last = $('*', $paginable).filter(function(index) {
				//	return $(this).data(beforeType);
				//}).last().data(beforeType);

				var items = $('*', $paginable).filter(function(index) {
					return $(this).data(beforeType);
				});
				var lastItem = top ? items.first() : items.last();
				var last = lastItem.data(beforeType);
				
				data = {
					before: last || null
				}
				
			} else {
				
				data = {
					page: nextPage
				}
			}
			
			$.ajax({
				url: url,
				dataType: 'json',
				data: data
			}).done(function(data) {
				if (data.length > 0) {
					$paginable.trigger('page-received', [data, nextPage]);
					$paginable.data('current-page', nextPage); 
				} else {
					$paginable.trigger('page-no-data');
				}
			}).fail(function() {
				$paginable.trigger('page-error');
			});
			
			return false;
		}
	})
	
	$('.paginable').each(function(i, elt) {
		var $paginable = $(elt);
		var btn = $($paginable.data('paginate-btn'));
		btn.click(function() {
			$paginable.paginate();
			return false;
		})
	});
	
	// PAGINATION ------
	
	// Boutons de login
	$('#login-button, .login-button').click(function() {
		loginProcess();
	});
	
	function focusOnLogin() {
		$('#login-modal input:first-child').focus();
	}
	
	$('#login-modal').on('shown', focusOnLogin);
	
	
	initLogoutLink();
	
});


function readCookie(name) {
    var nameEQ = name + "=";
    var ca = document.cookie.split(';');
    for (var i = 0; i < ca.length; i++) {
        var c = ca[i];
        while (c.charAt(0) == ' ') c = c.substring(1, c.length);
        if (c.indexOf(nameEQ) == 0) return c.substring(nameEQ.length, c.length);
    }
    return null;
}

// Geolocation

herbonautes.saveUserLocation = function(login, latitude, longitude) {

	var url = herbonautes.ctxPath + '/users/' + login + '/location';
	$.ajax({
		type: 'POST',
		url: url,
		data: {
			latitude: latitude,
			longitude: longitude
		}
	});
}

herbonautes.getGeolocation = function(login) {

	console.log("Get geolocation");

    if (readCookie("H_LOCATION_SAVED") == "true") {
        console.log("Already saved");
        return;
    }

    function onLocation(geolocation) {
		var lat = geolocation.coords.latitude,
			lng = geolocation.coords.longitude;
		herbonautes.saveUserLocation(login, lat, lng);
	}

	function onLocationError(geolocation) {
		console.log("User disabled geolocation");
        herbonautes.saveUserLocation(login, null, null);
	}

	if (typeof(navigator) !== 'undefined' && navigator.geolocation) {
		navigator.geolocation.getCurrentPosition(onLocation, onLocationError);
	}
}

