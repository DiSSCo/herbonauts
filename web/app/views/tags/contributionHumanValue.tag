
#{set first_val: true /}

#{list items: _answer.toHumanValue(), as: 'val'}
        *{ #{if val.label && !val.isBoolean}
            ${val.label} :
        #{/if} }*


        #{if !val.isBoolean || val.booleanValue}

            #{if !first_val}<br/>#{/if}
            #{if val.isBoolean}
                #{if val.booleanValue}
                    <em>${val.label}</em>
                #{/if}
            #{/if}
            #{else}
                ${val.textValue}
            #{/else}

            #{set first_val: false /}
        #{/if}
#{/list}

