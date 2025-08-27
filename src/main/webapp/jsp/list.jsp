<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<!DOCTYPE html>
<html lang="ja">
<head>
<meta charset="UTF-8">
<title>予約一覧</title>
<link rel="stylesheet" href="<c:url value='/style.css' />">
</head>
<body>
  <div class="container">
    <h1>予約一覧</h1>

    <!-- 検索・ソート -->
    <form action="<c:url value='/reservation' />" method="get" class="search-sort-form">
      <input type="hidden" name="action" value="list">
      <div>
        <label for="search">検索:</label>
        <input type="text" id="search" name="search"
               value="<c:out value='${searchTerm}'/>"
               placeholder="名前または日時">
      </div>
      <div>
        <label for="sortBy">ソート基準:</label>
        <select id="sortBy" name="sortBy">
          <option value="" <c:if test="${sortBy == null || sortBy == ''}">selected</c:if>>選択してください</option>
          <option value="name" <c:if test="${sortBy == 'name'}">selected</c:if>>名前</option>
          <option value="time" <c:if test="${sortBy == 'time'}">selected</c:if>>日時</option>
        </select>
      </div>
      <div>
        <label for="sortOrder">ソート順:</label>
        <select id="sortOrder" name="sortOrder">
          <option value="asc"  <c:if test="${sortOrder == 'asc'}">selected</c:if>>昇順</option>
          <option value="desc" <c:if test="${sortOrder == 'desc'}">selected</c:if>>降順</option>
        </select>
      </div>
      <button type="submit" class="button">検索/ソート</button>
    </form>

    <!-- メッセージ -->
    <p class="error-message"><c:out value="${errorMessage}" /></p>
    <p class="success-message"><c:out value="${successMessage}" /></p>

    <!-- CSV操作/クリーンアップ -->
    <div class="button-group">
      <a href="<c:url value='/reservation'><c:param name='action' value='export_csv'/></c:url>" class="button">CSV エクスポート</a>

      <form action="<c:url value='/reservation' />" method="get" style="display:inline;">
        <input type="hidden" name="action" value="clean_up" />
        <input type="submit" value="過去の予約をクリーンアップ" class="button secondary"
               onclick="return confirm('本当に過去の予約を削除しますか？');">
      </form>
    </div>

    <!-- 一覧テーブル -->
    <table>
      <thead>
        <tr>
          <th>ID</th>
          <th>名前</th>
          <th>予約日時</th>
          <th>操作</th>
        </tr>
      </thead>
      <tbody>
        <c:forEach var="reservation" items="${reservations}">
          <tr>
            <td>${reservation.id}</td>
            <td>${reservation.name}</td>
            <td>${reservation.reservationTime}</td>
            <td class="table-actions">
              <c:url var="editUrl" value="/reservation">
                <c:param name="action" value="edit"/>
                <c:param name="id" value="${reservation.id}"/>
              </c:url>
              <a href="${editUrl}" class="button">編集</a>

              <form action="<c:url value='/reservation' />" method="post" style="display:inline;">
                <input type="hidden" name="action" value="delete">
                <input type="hidden" name="id" value="${reservation.id}">
                <input type="submit" value="キャンセル" class="button danger"
                       onclick="return confirm('本当にキャンセルしますか？');">
              </form>
            </td>
          </tr>
        </c:forEach>
        <c:if test="${empty reservations}">
          <tr>
            <td colspan="4">予約がありません。</td>
          </tr>
        </c:if>
      </tbody>
    </table>

    <!-- ページネーション -->
    <div class="pagination">
      <c:if test="${currentPage != 1}">
        <c:url var="prevUrl" value="/reservation">
          <c:param name="action" value="list"/>
          <c:param name="page" value="${currentPage - 1}"/>
          <c:param name="search" value="${searchTerm}"/>
          <c:param name="sortBy" value="${sortBy}"/>
          <c:param name="sortOrder" value="${sortOrder}"/>
        </c:url>
        <a href="${prevUrl}">前へ</a>
      </c:if>

      <c:forEach begin="1" end="${noOfPages}" var="i">
        <c:choose>
          <c:when test="${currentPage eq i}">
            <span class="current">${i}</span>
          </c:when>
          <c:otherwise>
            <c:url var="pageLink" value="/reservation">
              <c:param name="action" value="list"/>
              <c:param name="page" value="${i}"/>
              <c:param name="search" value="${searchTerm}"/>
              <c:param name="sortBy" value="${sortBy}"/>
              <c:param name="sortOrder" value="${sortOrder}"/>
            </c:url>
            <a href="${pageLink}">${i}</a>
          </c:otherwise>
        </c:choose>
      </c:forEach>

      <c:if test="${currentPage lt noOfPages}">
        <c:url var="nextUrl" value="/reservation">
          <c:param name="action" value="list"/>
          <c:param name="page" value="${currentPage + 1}"/>
          <c:param name="search" value="${searchTerm}"/>
          <c:param name="sortBy" value="${sortBy}"/>
          <c:param name="sortOrder" value="${sortOrder}"/>
        </c:url>
        <a href="${nextUrl}">次へ</a>
      </c:if>
    </div>

    <div class="button-group">
      <a href="<c:url value='/index.jsp' />" class="button secondary">トップに戻る</a>
    </div>
  </div>
</body>
</html>
