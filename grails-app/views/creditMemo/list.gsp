<%@ page import="org.amcworld.springcrm.CreditMemo" %>
<html>
<head>
  <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
  <meta name="layout" content="main" />
  <g:set var="entityName" value="${message(code: 'creditMemo.label', default: 'CreditMemo')}" />
  <g:set var="entitiesName" value="${message(code: 'creditMemo.plural', default: 'CreditMemos')}" />
  <title>${entitiesName}</title>
  <r:require modules="invoicingTransaction" />
</head>

<body>
  <div id="main-container-header">
    <h2><g:message code="${entitiesName}" /></h2>
    <nav id="toolbar-container">
      <ul id="toolbar">
        <li><g:link action="create" class="green"><g:message code="default.new.label" args="[entityName]" /></g:link></li>
      </ul>
    </nav>
  </div>
  <section id="content">
    <g:if test="${flash.message}">
    <div class="flash-message message" role="status">${flash.message}</div>
    </g:if>
    <g:if test="${creditMemoInstanceList}">
    <table class="content-table">
      <thead>
        <tr>
          <th scope="col"><input type="checkbox" id="credit-memo-row-selector" /></th>
          <g:sortableColumn scope="col" property="number" title="${message(code: 'invoicingTransaction.number.label', default: 'Number')}" />
          <g:sortableColumn scope="col" property="subject" title="${message(code: 'invoicingTransaction.subject.label', default: 'Subject')}" />
          <g:sortableColumn scope="col" property="organization.name" title="${message(code: 'invoicingTransaction.organization.label', default: 'Organization')}" />
          <g:sortableColumn scope="col" property="stage" title="${message(code: 'creditMemo.stage.label.short', default: 'Stage')}" />
          <g:sortableColumn scope="col" property="docDate" title="${message(code: 'creditMemo.docDate.label.short', default: 'Date')}" />
          <g:sortableColumn scope="col" property="paymentDate" title="${message(code: 'invoicingTransaction.paymentDate.label', default: 'Payment date')}" />
          <g:sortableColumn scope="col" property="total" title="${message(code: 'creditMemo.total.label.short', default: 'Total')}" />
          <g:sortableColumn scope="col" property="closingBalance" title="${message(code: 'invoicingTransaction.closingBalance.label', default: 'Closing balance')}" />
          <th scope="col"></th>
        </tr>
      </thead>
      <tbody>
      <g:each in="${creditMemoInstanceList}" status="i" var="creditMemoInstance">
        <tr>
          <td class="row-selector"><input type="checkbox" id="credit-memo-row-selector-${creditMemoInstance.id}" data-id="${creditMemoInstance.id}" /></td>
          <td class="id credit-memo-number"><g:link action="show" id="${creditMemoInstance.id}"><g:fieldValue bean="${creditMemoInstance}" field="fullNumber" /></g:link></td>
          <td class="string credit-memo-subject"><g:link action="show" id="${creditMemoInstance.id}"><g:fieldValue bean="${creditMemoInstance}" field="subject" /></g:link></td>
          <td class="ref credit-memo-organization"><g:link controller="organization" action="show" id="${creditMemoInstance.organization?.id}"><g:fieldValue bean="${creditMemoInstance}" field="organization" /></g:link></td>
          <td class="status credit-memo-stage payment-state payment-state-${creditMemoInstance?.paymentStateColor}"><g:fieldValue bean="${creditMemoInstance}" field="stage" /></td>
          <td class="date credit-memo-doc-date"><g:formatDate date="${creditMemoInstance?.docDate}" formatName="default.format.date" /></td>
          <td class="date credit-memo-payment-date"><g:formatDate date="${creditMemoInstance?.paymentDate}" formatName="default.format.date" /></td>
          <td class="currency credit-memo-total"><g:formatCurrency number="${creditMemoInstance?.total}" displayZero="true" /></td>
          <td class="currency credit-memo-closing-balance balance-state balance-state-${creditMemoInstance?.balanceColor}"><g:formatCurrency number="${creditMemoInstance?.closingBalance}" displayZero="true" /></td>
          <td class="action-buttons">
            <g:if test="${session.user.admin || creditMemoInstance.stage.id < 2502}">
            <g:link action="edit" id="${creditMemoInstance.id}" class="button small green"><g:message code="default.button.edit.label" /></g:link>
            <g:link action="delete" id="${creditMemoInstance?.id}" class="button small red delete-btn"><g:message code="default.button.delete.label" /></g:link>
            </g:if>
            <g:else>
            <g:link action="editPayment" id="${creditMemoInstance.id}" class="button small green"><g:message code="invoicingTransaction.button.editPayment.label" /></g:link>
            </g:else>
          </td>
        </tr>
      </g:each>
      </tbody>
    </table>
    <div class="paginator">
      <g:paginate total="${creditMemoInstanceTotal}" />
    </div>
    </g:if>
    <g:else>
      <div class="empty-list">
        <p><g:message code="default.list.empty" /></p>
        <div class="buttons">
          <g:link action="create" class="green"><g:message code="default.new.label" args="[entityName]" /></g:link>
        </div>
      </div>
    </g:else>
  </section>
</body>
</html>
