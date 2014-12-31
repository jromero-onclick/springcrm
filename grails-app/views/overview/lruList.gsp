<div class="panel-body">
  <ul>
    <g:each in="${lruList}" var="lruEntry">
    <li>
      <g:dataTypeIcon controller="${lruEntry.controller}" />
      <div class="text">
        <g:link controller="${lruEntry.controller}" action="show"
          id="${lruEntry.itemId}">${lruEntry.name}</g:link>
        (<g:message code="${lruEntry.controller}.label"
          default="${lruEntry.controller}" />)
      </div>
      <div class="buttons">
        <g:link controller="${lruEntry.controller}" action="edit"
          id="${lruEntry.itemId}"
          params="[returnUrl: createLink(uri: '/', absolute: true)]"
          title="${message(code: 'default.btn.edit')}"
          ><i class="fa fa-pencil-square-o"></i
          ><span class="sr-only"><g:message code="default.btn.edit" /></span
        ></g:link>
        <%--
        <a href="#" class="text-danger" title="Aus Liste entfernen"><i class="fa fa-close"></i><span class="sr-only">Aus Liste entfernen</span></a>
        --%>
      </div>
    </li>
    </g:each>
  </ul>
</div>
