*{~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
  
	Tag liste spécimens
    
  ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~}*

<div id="specimensList" ng-controller="specimensListCtrl" ng-init="init('${_tagLabel}')" class="row-fluid">

    <p ng-hide="specimens.length > 0" class="noResults">
        &{'no.result'}
    </p>
    <div ng-repeat="specimen in specimens" id="specimen{{specimen.id}}" class="specimen-box-index">
        <table class="specimen-table">
            <tr>
                <td class="avatar">
                    <a ng-href="{{getSpecimenUrl(specimen)}}" >
                        <img alt="&{'specimen.of', ''} {{specimen.family}} {{specimen.genus}}" ng-src="${_herbonautes.tilesRootURL}{{specimen.institute}}/{{specimen.collection}}/{{specimen.code}}/tile_0_0_0.jpg" />
                    </a>
                </td>
                <td class="description">
                    <h4 class="specimen-title">
                        <a ng-href="{{getSpecimenUrl(specimen)}}" >{{specimen.institute}} / {{specimen.collection}} / {{specimen.code}}</a>
                    </h4>
                    <h2 class="specimen-scientific-name">
                        <a ng-href="{{getSpecimensListUrl(specimen)}}" >{{specimen.getGenusSpecies()}}</a>
                    </h2>

                </td>
                <td class="buttons">
                    <div class="button">
                        <a class="btn btn-primary" ng-href="{{getSpecimenUrl(specimen)}}" >&{'tag.specimen.consult'}</a>
                    </div>
                </td>
            </tr>
        </table>
    </div>
</div>