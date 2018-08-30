function displayUnusable(id) {
	$('#mark-unusable-btn').hide();
	$('#unmark-unusable-btn').show();
	$('.contribution-box').hide();
	$('#contribution-unusable-box').show();
	$('#unmark-unusable-btn').attr('href', deleteContributionAction({id: id}));
	//computeNextSpecimen();
}

function clearUnusable() {
	$('#unmark-unusable-btn').hide();
	$('#mark-unusable-btn').show();
	$('#contribution-unusable-box').hide();
	$('.contribution-box').show();
	$('#unmark-unusable-btn').attr('href', '#');
}

function markContributionToPost(type) {
	var $box = $('[data-type="' + type + '"]');
	$box.addClass('to-submit');
}
function unmarkContributionToPost(type) {
	var $box = $('[data-type="' + type + '"]');
	$box.removeClass('to-submit');
}
function getContributionTypeToPost() {
	var $box = $('.to-submit');
	return $box.data('type');
}

function focusOnFirstContribution() {
	var $box = $('.contribution-box')
		.not('.done')
		.not('.complete')
		.not('.lock')
		.first();
	
	$box
		.openBox()
		.find('input[type="text"], select')
		.first()
		.focus();
	
	/*
	if ($box.length > 0) { 
		$('#left-bar')
			.scrollTop($box.prev().position().top - 300);
	}
	*/
}
function isSpecimenComplete() {
	var $emptyboxes = $('.contribution-box.empty').not('.lock');
	return ($emptyboxes.length == 0);
}
function displayCompleteSpecimen() {
	if (isSpecimenComplete()) {
		//computeNextSpecimen();
		$('#complete-specimen').show();
	} else {
		$('#complete-specimen').hide();
	}
}


function onPostContributionError(type, jqXHR) {
	//alert('TYPE ' + type);
	if (jqXHR.status == 400) { // Droits OK mais requete invalide
		//alert('[400]');
		var $box = $('[data-type="' + type + '"]');
		var errors = $.parseJSON(jqXHR.responseText);
		displayErrors($box, errors);
		$box.hideLoading();
	} else if (jqXHR.status == 403) {
		//alert('[403] ' + jqXHR.responseText);
		markContributionToPost(type);
		var _postAgain = function() {
			postContribution(type);
		}
		var _stopPost = function() {
			var $box = $('[data-type="' + type + '"]');
			$box.hideLoading();
		}
		if ('not.connected' == jqXHR.responseText) {
			//alert('call login process');
			//_connect(_postAgain);
			$.when(_connect(type)).done(_postAgain).fail(function() {
				var $box = $('[data-type="' + type + '"]');
				$box.hideLoading();
			});
			//.fail(_stopPost);
		} else if ('not.member' == jqXHR.responseText) {
			//alert('call join mission process');
			$.when(_join()).done(_postAgain).fail(function() {
				var $box = $('[data-type="' + type + '"]');
				$box.hideLoading();
			});
			//.fail(_stopPost);
		} else {
			$('#login-modal #login-error').show();
			$.when(_connect(type)).done(_postAgain).fail(function() {
				var $box = $('[data-type="' + type + '"]');
				$box.hideLoading();
			});
		}
		//var typeToPost = getContributionTypeToPost();
		//alert('re post ' + typeToPost);
	} else {
		errorAlert();
	}
}

onUserConnectedAfter = function() {
	$.getJSON(levelInfosAction(), function(json) {
		onUserConnected(json.level, json.pendingLevel, currentSpecimenID);
	});
	markCurrentMissionAndSpecimen();
}


function _connect(type) {
	
	var _connectDeferred = $.Deferred();
	
	herbonautes.onSuccessFB = function() {
		_connectDeferred.resolve();
	}
	
	$('#login-modal').on('hide', function() {
		$contributionBox(type).hideLoading();
	});
	
	$('#login-form').submit(function() {
		var $form = $(this);
		var ok = false;
		$.ajax({
			type: 'POST',
			url: $form.attr('action'),
			data: $form.serialize(),
			success: function(user) {
				fillProfileMenu(user);
				fillMissionsMenu(user);
				$('#login-modal').modal('hide');
				_connectDeferred.resolve();
				onUserConnectedAfter();
			},
			error: function(jqXHR, textStatus, errorThrown) {
				onPostContributionError(type, jqXHR);
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
				onUserConnectedAfter();
				$('#login-modal').modal('hide');
				signedUpAlert();
				//alert('Vous etes maintenant inscrit'); // ICI ON MET A JOUR LE MENU
				_connectDeferred.resolve();
			},
			error: function(jqXHR, textStatus, errorThrown) {
				onSignupError(jqXHR);
			}
		});
		return false;
	});
		
	/*$('#login-modal').on('hide', function() {
		_connectDeferred.reject();
	});*/
	
	$('#login-modal').modal('show');
	
	return _connectDeferred;
} 

/*
function _connect() {
	return $.ajax({
		url: 
	});
}
*/
function _join() {
	var _joinDeferred = $.Deferred();
	
	//alert('join');
	joinMissionProcess(_joinDeferred);
	
	return _joinDeferred;
}

function needContribution(type) {
	var $box = $contributionBox(type);
	if ($box.length == 0) {
		return false;
	} else {
		return !$box.hasClass('done');
	}
}

function postContribution(type) {
	
	
	if (type == 'UNUSABLE') {
		$('#unusable-form').submit();
		return;
	} 
	
	if (type == 'REGION_1' && needContribution('COUNTRY')) {
		postContribution('COUNTRY')
	} else if (type == 'REGION_2' && needContribution('REGION_1')) {
		postContribution('REGION_1')
	}
	
	var $box = $('[data-type="' + type + '"]');
	var $form = $('.contribution-form', $box);
	$box.showLoading();
	
	$.ajax({
		type: 'POST',
		url: $form.attr('action'),
		data: $form.serialize(),
		success: function(response) {
			$box.displayContribution(response);
			focusOnFirstContribution();
			displayCompleteSpecimen();
			$box.hideLoading();
		},
		error: function(jqXHR, textStatus, errorThrown) {
			onPostContributionError(type, jqXHR);
		}
	});
	
}

function $contributionBox(type) {
	return $('.contribution-box[data-type="' + type + '"]');
}

function deleteContribution(type) {
	var $box = $contributionBox(type);
	var id = $box.data('contribution-id');
	
	if (!id) {
		return false;
	}
	
	$box.showLoading();
	
	var url = deleteContributionAction({id: id});
	
	$.ajax({
		type: 'GET',
		url: url,
		success: function(response) {
			$box.clearContribution();
			displayReport_(response.report);
			$box.hideLoading();
			focusOnFirstContribution();
			displayCompleteSpecimen();
		}
	});
	
	var cascade;
	if (cascade = $box.data('cascade')) {
		deleteContribution(cascade);
	}
	
}

function joinMissionProcess(deferred) {
	
	//alert('join mission process');
	
	$('#join-modal').on('hide', function() {
		$('#join-mission-button').unbind('click'); 
	})
	
	$('#join-mission-button').click(function() {
		herbonautes.joinMission({
			success: function() {
				deferred.resolve();
				$('#join-modal').modal('hide');
			},
			error: function() {
				errorAlert();
			}
		});
		
	});
	
	$('#join-modal').modal('show');
} 

/*
function contributionAddError(jqXHR, $box) {
	if (jqXHR.status == 400) {
		var errors = $.parseJSON(jqXHR.responseText);
		displayErrors($box, errors);
		$box.hideLoading();
	} else if (jqXHR.status == 403) {
		if ('not.member' == jqXHR.responseText) {
			joinMissionProcess($box);
		} else { 
			loginProcess(function() {
				postContribution($box.data('type'));
				return false;
			});
		}
	} else {
		alert('erreur inconnue : ' + jqXHR.status);
	}
	$box.hideLoading();
}
*/

function validateContribution(id, type) {
	var $box = $contributionBox(type);
	if (!$box) {
		return false;
	}
	var url = validateContributionAction({id: id});
	$('#conflicts-modal').modal('hide');
	$box.showLoading();
	$.ajax({
		type: 'POST',
		url: url,
		async: false,
		success: function(response) {
			$box.displayContribution(response);
			$box.hideLoading();
			focusOnFirstContribution();
			displayCompleteSpecimen();
		},
		error: function() {
			errorAlert();
			$box.hideLoading();
		}
	});
	return false;
}

function keepContribution(id, type) {
	var $box = $contributionBox(type);
	if (!$box) {
		return false;
	}
	var url = keepContributionAction({id: id});
	$('#conflicts-modal').modal('hide');
	$box.showLoading();
	$.ajax({
		type: 'POST',
		url: url,
		async: false,
		success: function(response) {
			$box.displayContribution(response);
			$box.hideLoading();
			focusOnFirstContribution();
			displayCompleteSpecimen();
		},
		error: function() {
			errorAlert();
			$box.hideLoading();
		}
	});
	return false;
}

// Mustache (templates déclarés au début de contributionBoard.html)
var _templateFor = function(type, value) {
	if (!herbonautes.templates.contributions || !herbonautes.templates.contributions[type]) {
		return null;
	}
	var templates = herbonautes.templates.contributions;
	if ('default' == value) {
		return templates[type];
	} else {
		// On cherche le par defaut, sinon specifique
		return templates[type][value] || templates[value];
	}
}

var _feedWithTemplate = function(elt, template, contribution) {
	$elt = $(elt);
	if (template) {
		$elt.html(Mustache.render(template, contribution));
	}
}

function createProposition_(contribution) {
	var $box = $contributionBox(contribution.type);
	var propBox = $('<div class="proposition-box" />');
	propBox.html($('.contribution-values', $box).html());
	$('[data-value]', propBox).each(function(i, elt) {
		$elt = $(elt);
		var template = _templateFor(contribution.type, $elt.data('value'));
		_feedWithTemplate(elt, template, contribution);
	});
	return propBox;
}

function displayErrors(box, errorMap) {
	$.each(errorMap, function(key, errorList) {
		$('p.error-message', box).text('');
		$.each(errorList, function(i, error) {
			$('[name="' + error.key + '"]', box)
				.addClass('error')
				.focus(function() {
					$(this).removeClass('error').unbind('focus');
					$('p.error-message', box).remove();
				});
				
				var message = false;
				$('p.error-message', box).each(function (i, elt) {
					if ($(elt).data('key') == error.key) {
						message = true;
					}
				})
				
				if (!message) {
					$('[name="' + error.key + '"]', box)
					.before(
							$('<p>')
							.data('key', error.key)
							.addClass('error-message')
							.text(i18n(error.message))
					);
				} else {
					var prev = $('p.error-message', box).text();
					$('p.error-message', box).text(prev + (prev == '' ? '' : ', ') + i18n(error.message));
				}
				
		});
	});
}

function displayContribution_(contribution) {

	if (contribution.type == 'UNUSABLE') {
		displayUnusable(contribution.id);
		return;
	} 
	
	var $box = $contributionBox(contribution.type);
	
	$box.data('contribution-id', contribution.id);
	
	// On masque les liens cachés
	$('.hidden-links', $box).hide();
	
	$('[data-value]', $box).each(function(i, elt) {
		$elt = $(elt);
		var template = _templateFor(contribution.type, $elt.data('value'));
		_feedWithTemplate(elt, template, contribution);
	})
	
	if (contribution.type == 'COUNTRY') {
		if (contribution.country) {
			updateRegionsLevel1(contribution.country.id);
		}
	} else if (contribution.type == 'REGION_1') {
		if (contribution.regionLevel1) {
			updateRegionsLevel2(contribution.regionLevel1.id);
		}
	}
	
	$box.find('.contribution-values').show();
	$box.removeClass('empty').addClass('done'); // On marque comme fait
	
	$box.trigger('done');
	
	$box.find('.remove-link').attr('href', deleteContributionAction({id: contribution.id}));
	
	$('.title', $box)
		.unbind('click')
		.unbind('mouseover')
		.unbind('click');
}

herbonautes.displayMyContributions = function(specimenId) {
	var url = contributionsForSpecimenAction({id: specimenId});
	return $.getJSON(url, function(contributions) {
		$.each(contributions, function(i, contribution) {
			displayContribution_(contribution);
		});
	});
}

herbonautes.lockContributions = function(level, pendingLevel) {
	return $('.contribution-box').each(function(i, elt) {
		var $box = $(this);
		var required = $box.data('level-required');
		
		if (level >= required) {
			if ($box.hasClass('lock') && required > 0) {
				$box.removeClass('lock');
				$box.removeClass('pending');
				$box.trigger('unlocked');
			}
		} else {
			$box.addClass('lock');
			var message = i18n('validation.required.level', required);
			//'Niveau ' + required + ' requis';
			 if (pendingLevel >= required) {
					if ($box.hasClass('lock') && required > 0) {
						$box.addClass('pending');
						$box.trigger('pending');
						
						// Retrieve quiz
						$.ajax({
							url: quizForLevelAction({level: required}),
							dataType: 'json',
							success: function(quiz) {
								var link = '<a href="' + startQuizAction({name: quiz.name}) +'">' + i18n('alert.pass.quiz', quiz.title); + '</a>';
								if ($('.lock-cause.quiz-available', $box).length == 0) {
									var type = $box.data('type');
									showNewQuizModal(quiz, type);
									$('.lock-cause', $box).addClass('quiz-available').html(link);
								}
							}
						})
						
						message = null;
						//'<a href="#">Passez le quiz pour débloquer le niveau</a>';
					}
			}
			if (message != null) {
				$('.lock-cause', this).html(message);
			}
		}
	});
}

function showNewQuizModal(quiz, type) {
	
	$('#quiz-modal .modal-body').text(i18n('quiz.modal.text', quiz.title, i18n('contribution.' + type.toLowerCase())));
	$('#quiz-modal #quiz-btn').attr('href', startQuizAction({name: quiz.name}));
	$('#quiz-modal').modal('show');
	
	//alert(quiz.title + ' for ' + type.toLowerCase());
	
}

function displayReport_(report, specimenId) {
	var $box = $contributionBox(report.type);
	console.log(report);
	console.log(report.type);
	
	if (report.complete) {
		$box.addClass('complete');
	} else {
		$box.removeClass('complete');
	}
	if (report.conflicts) {
		$box.addClass('conflicts');
		$box.children('.conflicts').click(function(){
			var url = contributionTypeForSpecimenAction({id: specimenId, type: report.type});
			return $.getJSON(url, function(feedback) {
				displayConflictsBox(feedback);
			});
		});
	} else {
		$box.removeClass('conflicts');
	}
	
	$('[data-report-value]', $box).each(function(i, elt) {
		$elt = $(elt);
		var contribution = report.validatedContribution;
		if (contribution) {
			var template = _templateFor(contribution.type, $elt.data('report-value'));
			_feedWithTemplate(elt, template, contribution);
		}
	})
	//alert('report : ' + report.validatedContribution.type);
}

herbonautes.displayContributionReports = function(specimenId) {
	var url = contributionReportsForSpecimenAction({id: specimenId});
	return $.getJSON(url, function(contributions) {
		$.each(contributions, function(i, report) {
			displayReport_(report, specimenId);
		});
	});
}

function updateRegionsLevel1(id) {
	var countryId = (typeof id === 'number' ?  id : $('#country-field').val());
	
	var url = regions1ForCountryAction({
		countryId: countryId
	});
	$('#regionLevel1-field').text('');
	$.ajax({
		type: 'GET',
		url: url,
		success: function(response) {
			$('<option />')
				.text(i18n('select.region'))
				.attr('value', '')
				.appendTo('#regionLevel1-field');
			$.each(response, function(i, region) {
				$('<option />')
					.attr('value', region.id)
					.text(region.name)
					.appendTo('#regionLevel1-field');
			});
		}
	});
}

function updateRegionsLevel2(id) {
	var region1Id = (typeof id === 'number' ?  id : $('#regionLevel1-field').val());
	
	var url = regions2ForRegion1Action({
		region1Id: region1Id
	});
	$('#regionLevel2-field').text('');
	$.ajax({
		type: 'GET',
		url: url,
		success: function(response) {
			$('<option />')
			.text(i18n('select.region'))
			.attr('value', '')
			.appendTo('#regionLevel2-field');
			$.each(response, function(i, region) {
				$('<option />')
					.attr('value', region.id)
					.text(region.name)
					.appendTo('#regionLevel2-field');
			});
		}
	});
}


$(function() {
		// Mise à jour des listes dépendantes
	
	// FERMEES PAR DEFAUT
	$('.contribution-box').addClass('closed');
	
	$('#country-field').change(updateRegionsLevel1);
	//updateRegionsLevel1();
	
	$('#regionLevel1-field').change(updateRegionsLevel2);
	//updateRegionsLevel2();
	
	// Affiche / masque le formulaire au clic sur le titre
	
	
	$.fn.disableToggleBox = function() {
		$('.title', this)
			.unbind('click')
			.unbind('mouseover')
			.unbind('click');
	}
	
	$.fn.enableToggleBox = function() {
	
	$('.title', this)
		.mouseover(function() {
			var $box = $(this).parents('.contribution-box');
			
			if ($box.is('.done, .lock')) {
				return false;
			}
			
			if ($box.is('.opened')) {
				$(this).find('.close-box-link').show();
				$(this).find('.open-box-link').hide();
			} else {
				$(this).find('.open-box-link').show();
				$(this).find('.close-box-link').hide();
			}
		})
		.mouseout(function() {
			$(this).find('.box-link').hide();
		})
		.click(function() {
			//alert($(this).parents('.contribution-box').attr('class'));
			var $box = $(this).parents('.contribution-box')
				.filter('.empty')
				.not('.lock');
			
			if ($box) {
				if ($box.is('.opened')) {
					$box.removeClass('opened');
					$box.addClass('closed');
				} else {
					$box.removeClass('closed');
					$box.addClass('opened');
				}
				$box.find('.title').mouseover();
			}
		});
	
	}
	
	$('.contribution-box.empty').each(function(i, elt) {
		$(elt).enableToggleBox();
	})
	
	// AJAXifions le formulaire photo inutilisable
	$('#unusable-form').submit(function() {
		var $form = $(this);
		$.ajax({
			type: 'POST',
			url: $form.attr('action'),
			data: $form.serialize(),
			dataType: 'json',
			success: function(id) {
				$form.parents('.modal').modal('hide');
				displayUnusable(id);
			},
			error: function(jqXHR, textStatus, errorThrown) {
				onPostContributionError('UNUSABLE', jqXHR);
			}
		});
		return false;
	})
	
	$('#unmark-unusable-btn').click(function() {
			var $link = $(this);
			$.ajax({
				type: 'GET',
				url: $link.attr('href'),
				success: clearUnusable
			})
			return false;
		});
	
	$('#unusable-form-btn').click(function() {
		$('#unusable-form').submit();
		return false;
	});
	
	// ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
	// AJAXify contribution forms
	// ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
	$('.contribution-box form').each(function (i, elt) {
		$(elt).submit(function(e) {		
			e.stopPropagation();
			
			// Remplace les - par des /
			$('.date-input', this).each(function (i, elt) {
				var d = $(elt);
				d.val(d.val().replace(/\-/g, '/'));
			})
			
			var $form = $(this);
			$form.trigger('clean');
			
			var $box = $form.parents('.contribution-box');
			
			postContribution($box.data('type'));
			
			return false;
		});
	});
	
	// ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
	// AJAXify remove links
	// ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
	$('.contribution-box .remove-link').each(function (i, elt) {
		$(elt).click(function() {
			var type = $(this).data('type');
			deleteContribution(type);
			return false;
		});
		
	});
	
	
	$('.contribution-box').mouseover(function() {
		$(this).addClass('hover');
		$(this).find('.hidden-links a').each(function(i, elt) {
			if ($(elt).attr('href') != '#') {
				$(elt).parent().show();
			}
		})
	}).mouseout(function() {
		$(this).removeClass('hover');
		$(this).find('.hidden-links').hide();
	});
	
	$('.contribution-box .hidden-links').hide();
	
	// TODO avec des .data (data-done, data-conflicts, data-completed)
	// Initialisation
	/*$('.contribution-box').each(function (i, elt) {
		var $box = $(elt);
		if ($box.hasClass('lock')) {
			$('.contribution-form', $box).hide();
			$('.contribution-values', $box).hide();
		} else if ($box.hasClass('done')) {
			$('.contribution-form', $box).hide();
			$('.contribution-values', $box).show();
		} else {
			$('.contribution-values', $box).hide();
			$('.contribution-form', $box).show();
		}
	});*/
	
	$.fn.showLoading = function() {
		var $box = $(this).filter('.contribution-box');
		var loading = $('.loading', $box);
		if (loading.length == 0) {
			$('<div class="loading"></div>').appendTo($box);
		} else {
			loading.show();
		}
		
	}
	
	$.fn.hideLoading = function() {
		var $box = $(this).filter('.contribution-box');
		$('.loading', $box).remove();
	}
	
	
	$.fn.unlockLevel = function(level) { 
		var $box = $(this);
		if (!$box.is('.contribution-box')) return;
		
		if ($box.data('level-required') == level) {
			$box.removeClass('lock');
		}
		return true;
	}
	
	//
	// <div class="value-group">
	//     <span id="value-name"></span> ou <span id="value-
	// </div>
	//
	$.fn.displayContribution = function(feedback) { 
		var $box = $(this);
		if (!$box.is('.contribution-box')) return;
		
		//$('.value-group', $box).hide();
		$('.hidden-links', $box).hide();
		
		var type = feedback.contribution.type;
		
		// Mustache (templates déclarés au début de contributionBoard.html)
		displayContribution_(feedback.contribution);
		//var template = TEMPLATES[type];
		//$('.value-not-present', $box).html(Mustache.render(TEMPLATES['NOT_PRESENT'], feedback.contribution));
		//$('.value-not-sure', $box).html(Mustache.render(TEMPLATES['NOT_SURE'], feedback.contribution));
		//$('.value-deducted', $box).html(Mustache.render(TEMPLATES['DEDUCTED'], feedback.contribution));
		//$('.value', $box).html(Mustache.render(template, feedback.contribution));
		
		// Affiche les conflits immédiats contribution avec les autres
		// (false après la validation d'une contribution pour ne pas boucler)
		if (feedback.conflicts && feedback.showConflicts) {
			displayConflictsBox(feedback);
		}
		
		// Conflits
		if (feedback.report && feedback.report.conflicts) {
			$box.addClass('conflicts');
		} else {
			$box.removeClass('conflicts');
		}
		
		if (feedback.report && feedback.report.complete) {
			$box.addClass('complete');
		} else {
			$box.removeClass('complete');
		}
		
		
		// TODO highlight ou notification + brancher quiz
		if (feedback.levelUp) {
			herbonautes.lockContributions(feedback.userLevel, feedback.userPendingLevel);
			/*$('.contribution-box').each(function (i, e) {
				$(e).unlockLevel(feedback.userLevel);
			});*/
		}
		
		
		if (feedback.attributes['badge.win']) {
			badgeAlert(feedback.attributes['badge.win']);
		}
		
		/*function(key, value) {
			var $val = $('#value-' + key, $box)
			$val.text(value).show();
			$val.parents('.value-group').show();
		});*/
		
		//$box.find('.contribution-form').hide();
		$box.closeBox();
		$box.find('.contribution-values').show();
		
		$box.find('.remove-link').attr('href', deleteContributionAction({id: feedback.contribution.id}));
		
		$box.removeClass('empty').addClass('done');
		
		$box.disableToggleBox();
    }
	
	$.fn.clearContribution = function() { 
		var $box = $(this).filter('.contribution-box');
		$box.openBox();
		$box.removeData('id');
		$('.contribution-values', $box).hide();
		$box.find('.hidden-links').hide();
		$box.find('.remove-link').attr('href', '#');
		
		$box.removeClass('done').addClass('empty');
		$box.trigger('empty');
		
		$box.enableToggleBox();
    }
	
	
	
	/*
	$('.contribution-box').bind('unlocked', function() {
		console.log('contribution ' + $(this).data('type') + ' unlocked on event');
	}).bind('pending', function() {
		console.log('contribution ' + $(this).data('type') + ' need quizz to unlock');
	});
	*/
	
});


// Construction de la fenetre de validation de contribution
function displayConflictsBox(report) {
	var	props = []
	$.each(report.byOthers, function(i, c) {
		var propBox = createProposition_(c);
		props[i] = '<tr="proposition">' +
			'<td class="user"><span class="avatar"><img src="' + herbonautes.actions.toUserImage({login: c.userLogin, imageId: c.userImageId || 'nopic' }) + '" /></span>' +
			'<a href="#">' + c.userLogin + '</a></td>' +
			'<td class="proposition-box">' +
			propBox.html() + 
			' </td>' + 
			'<td>' +
			' <a href="#" class="btn btn-small" onclick="validateContribution(\'' + c.id + '\', \'' + c.type + '\')">' + i18n('btn.validate') + '</a>' +
			'</div>' +
			'</td></tr>';
		//list.append($('<li/>').text(c.userLogin + ' propose ' + c.notPresent + ': '+ c.country + ' : ' + c.id));
	});
	
	$('#conflicts-modal #propositions').html(props.join(''));
	
	$('#conflicts-modal #keep-btn, #conflicts-modal #close-conflicts').unbind('click');
	$('#conflicts-modal #keep-btn, #conflicts-modal #close-conflicts').click(function() {
		keepContribution(report.contribution.id, report.contribution.type);
	});
	
	$('#conflicts-modal').modal('show');
}


function errorAlert(message) {
	$('#error-modal').modal('show');
}
function signedUpAlert() {
	$('#signedup-modal').modal('show');
}

