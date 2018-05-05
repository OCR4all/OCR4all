/**
 * Includes project specific jQuery recognition model selection functionality
 * 
 * Things that need to be done to use this:
 * 1. Include this file in the head of the target page (see head.tag)
 *    In this project it can be included by passing recModelSelect="true" to the head.tag include
 * 2. Use initializeRecModelSelect function to create multi-select element
 */

var recModelSelectOptions = {
    selectableFooter: "<div class='custom-header'><button data-id='msSelectAll' class='btn'>Add all</button></div>",
    selectionFooter: "<div class='custom-header'><button data-id='msDeSelectAll' class='btn'>Remove all</button></div>",
    selectableHeader: "<input type='text' class='search-input' autocomplete='off' placeholder='Search'>",
    selectionHeader: "<input type='text' class='search-input' autocomplete='off' placeholder='Search'>",
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
    },
    afterDeselect: function(){
      this.qs1.cache();
      this.qs2.cache();
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
    $('body').on('click', 'button[data-id="msSelectAll"]', function() {
        $(this).parents('.ms-container').prev().multiSelect('select_all');
        return false;
    });

    $('body').on('click', 'button[data-id="msDeSelectAll"]', function() {
        $(this).parents('.ms-container').prev().multiSelect('deselect_all');
        return false;
    });
});
