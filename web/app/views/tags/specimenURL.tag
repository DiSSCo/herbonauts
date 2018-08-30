#{if _absoluteURL}@@{Specimens.show(institute: _arg.institute, collection: _arg.collection, code: _arg.code)}#{/if}
#{else}@{Specimens.show(institute: _arg.institute, collection: _arg.collection, code: _arg.code)}#{/else}
