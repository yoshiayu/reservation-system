<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<!DOCTYPE html>
<html lang="ja">
<head>
<meta charset="UTF-8">
<title>予約編集</title>
<link rel="stylesheet" href="<c:url value='/style.css' />">
</head>
<body>
	<div class="container">
		<h1>予約編集</h1>
		<form action="${pageContext.request.contextPath}/reservation" method="post">
			<input type="hidden" name="action" value="update"> <input
				type="hidden" name="id" value="${reservation.id}">
			<p>
				<label for="name">名前:</label> <input type="text" id="name"
					name="name" value="<c:out value="${reservation.name}"/>" required>
				<span class="error-message"><c:out value="${errorMessage}" /></span>
			</p>
			<p>
				<label for="reservation_time">希望日時:</label> <input
					type="datetime-local" id="reservation_time" name="reservation_time"
					value="<c:out
value="${reservation.reservationTime}"/>" required>
				<span class="error-message"><c:out value="${errorMessage}" /></span>
			</p>
			<div class="button-group">
				<input type="submit" value="更新"> 
				<a href="${pageContext.request.contextPath}/reservation?action=list" class="button secondary">予約一覧に戻る</a>
			</div>
		</form>
	</div>
</body>
</html>