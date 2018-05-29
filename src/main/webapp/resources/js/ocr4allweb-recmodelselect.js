/**
 * Includes project specific jQuery recognition model selection functionality
 * 
 * Things that need to be done to use this:
 * 1. Include this file in the head of the target page (see head.tag)
 *    In this project it can be included by passing recModelSelect="true" to the head.tag include
 * 2. Use initializeRecModelSelect function to create multi-select element
 */

var recModelSelectOptions = {
    // Layout (header + footer)
    selectableFooter: "<div class='custom-header'>" +
            "<button data-id='msRecModelSelectAll' class='btn'>Select all</button>" +
            "<button data-id='msRecModelAddOption' class='btn' style='margin-left: 5px;'>Add model</button>" +
        "</div>",
    selectionFooter: "<div class='custom-header'><button data-id='msRecModelDeSelectAll' class='btn'>Remove all</button></div>",
    selectableHeader: "<span class='msHeading'>Available</span><input type='text' class='search-input' autocomplete='off' placeholder='Search'>",
    selectionHeader: "<span class='msHeading'>Selected</span><input type='text' class='search-input' autocomplete='off' placeholder='Search'>",

    // Search functionality
    afterInit: function(ms){
      var that = this,
          $selectableSearch = that.$selectableUl.prev(),
          $selectionSearch = that.$selectionUl.prev(),
          selectableSearchString = '#'+that.$container.attr('id')+' .ms-elem-selectable:not(.ms-selected)',
          selectionSearchString = '#'+that.$container.attr('id')+' .ms-elem-selection.ms-selected';

      that.qs1 = $selectableSearch.quicksearch(selectableSearchString)
      .on('keydown', function(e){
        if (e.which === 40){
          that.$selectableUl.focus();
          return false;
        }
      });
      that.qs2 = $selectionSearch.quicksearch(selectionSearchString)
      .on('keydown', function(e){
        if (e.which == 40){
          that.$selectionUl.focus();
          return false;
        }
      });
    },

    afterSelect: function(){
      this.qs1.cache();
      this.qs2.cache();
      // Error handling (do not allow empty --checkpoint list)
      validateCheckpoints();
    },
    afterDeselect: function(){
      this.qs1.cache();
      this.qs2.cache();
      // Error handling (do not allow empty --checkpoint list)
      validateCheckpoints();
    },
};

//Error handling (indicate empty --checkpoint list)
function validateCheckpoints() {
    var multiSelectContainer = $('button[data-id="msRecModelAddOption"]').parents('.ms-container');
    var multiSelectSelectionList = $(multiSelectContainer).find('.ms-selection').find('.ms-list');
    var multiSelect = $(multiSelectContainer).prev();

    if( $(multiSelect).val() && $(multiSelect).val().join(" ").length > 0 ) {
        $(multiSelectSelectionList).removeClass('invalid');
    }
    else {
        $(multiSelectSelectionList).addClass('invalid');
    }
};

function initializeRecModelSelect(multiSelectId) {
    $.get( 'ajax/recognition/listModels' )
    .done(function( data ) {
        $.each(data, function(key, value) {
            var optionEl = $("<option></option>").attr("value", value).text(key);
            $(multiSelectId).append(optionEl);
        });
        $(multiSelectId).multiSelect(recModelSelectOptions);
    });
};

$(document).ready(function() {
    // Functions to add/remove all models at once
    $('body').on('click', 'button[data-id="msRecModelSelectAll"]', function() {
        $(this).parents('.ms-container').prev().multiSelect('select_all');
        return false;
    });
    $('body').on('click', 'button[data-id="msRecModelDeSelectAll"]', function() {
        $(this).parents('.ms-container').prev().multiSelect('deselect_all');
        return false;
    });

    // Add new models to list
    $('body').on('click', 'button[data-id="msRecModelAddOption"]', function() {
        $('#modal_recaddmodel').modal('open');
        return false;
    });
    $('body').on('click', '#addRecModel', function() {
        var modelName = $('#recModelName').val();
        var modelPath = $('#recModelPath').val();
        var multiSelect = $('button[data-id="msRecModelAddOption"]').parents('.ms-container').prev();

        // No empty input allowed
        if (modelName == "" || modelPath == "") {
            if (modelName == "") {
                $('#recModelName').addClass('invalid').focus();
                if (modelPath == "")
                    $('#recModelPath').addClass('invalid');
            }
            else if (modelPath == "") {
                $('#recModelPath').addClass('invalid').focus();
            }

            return false;
        }

        // No duplicate input allowed
        var duplicates = false;
        $.each($('[data-setting="--checkpoint"] option'), function(key, el) {
            if (escape($(el).text()) === escape(modelName) || escape($(el).val()) === escape(modelPath)) {
                if (escape($(el).text()) === escape(modelName)) {
                    $('#recModelName').addClass('invalid').focus();
                    if (escape($(el).val()) === escape(modelPath))
                        $('#recModelPath').addClass('invalid');
                }
                else if (escape($(el).val()) === escape(modelPath)) {
                    $('#recModelPath').addClass('invalid').focus();
                }

                duplicates = true;
                return false;
            }
        });
        if (duplicates) return false;

        // All checks passed, add new model
        $(multiSelect).multiSelect('addOption', { value: modelPath, text: modelName, index: 0 });
        $('#modal_recaddmodel').modal('close');
        return false;
    });
});
