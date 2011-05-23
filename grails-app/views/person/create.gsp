

<%@ page import="org.amcworld.springcrm.Person" %>
<html>
<head>
  <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
  <meta name="layout" content="main" />
  <g:set var="entityName" value="${message(code: 'person.label', default: 'Person')}" />
  <g:set var="entitiesName" value="${message(code: 'person.plural', default: 'Persons')}" />
  <title><g:message code="default.create.label" args="[entityName]" /></title>
</head>

<body>
  <div id="main-container-header">
    <h2><g:message code="${entitiesName}" /></h2>
    <nav id="toolbar-container">
      <ul id="toolbar">
        <li><a href="javascript:void 0;" class="green" onclick="springcrm.onClickSubmit('person-form');"><g:message code="default.button.save.label" /></a></li>
        <li><g:link action="list" class="red"><g:message code="default.button.cancel.label" /></g:link></li>
      </ul>
    </nav>
  </div>
  <section id="content">
    <g:if test="${flash.message}">
    <div class="flash-message message">${flash.message}</div>
    </g:if>
    <g:hasErrors bean="${personInstance}">
    <div class="flash-message form-error-hint"><g:message code="default.form.errorHint" /></div>
    </g:hasErrors>
    <h3><g:message code="person.new.label" default="New ${entityName}" /></h3>
    <g:form name="person-form" action="save" >
      <g:render template="/person/form" />
    </g:form>
  </section>
  <content tag="jsTexts">
  copyAddressWarning_mailingAddr: "${message(code: 'person.otherAddr.exists')}",
  copyAddressWarning_otherAddr: "${message(code: 'person.mailingAddr.exists')}"
  </content>
</body>
</html>
