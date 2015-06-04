<div class="row">
  <div class="ticket-log-entry-label">
    <g:message code="ticket.messageText.label" />
  </div>
  <div class="ticket-log-entry-value">
    <div class="html-content"
      ><markdown:renderHtml text="${logEntry.message}"
    /></div>
  </div>
</div>
<g:if test="${logEntry.attachment}">
<div class="row">
  <div class="ticket-log-entry-label">
    <g:message code="ticket.attachment.label" />
  </div>
  <div class="ticket-log-entry-value">
    <g:link controller="dataFile" action="loadFile"
      id="${logEntry.attachment.id}"
      params="[type: 'ticketMessage']" target="_blank">
      <g:fieldValue bean="${logEntry.attachment}" field="fileName"/>
    </g:link>
    (<g:formatSize number="${logEntry.attachment.fileSize}" />)
  </div>
</div>
</g:if>
