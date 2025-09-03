package com.example.reservation;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.Part;

@MultipartConfig
@WebServlet("/reservation")
public class ReservationServlet extends HttpServlet {
	private final ReservationDAO reservationDAO = new ReservationDAO();

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		String action = req.getParameter("action");
		if ("list".equals(action) || action == null) {
			String searchTerm = req.getParameter("search");
			String sortBy = req.getParameter("sortBy");
			String sortOrder = req.getParameter("sortOrder");
			int page = 1;
			int recordsPerPage = 5;
			if (req.getParameter("page") != null) {
				page = Integer.parseInt(req.getParameter("page"));
			}
			List<Reservation> allReservations = reservationDAO.searchAndSortReservations(searchTerm, sortBy,
					sortOrder);
			int start = (page - 1) * recordsPerPage;
			int end = Math.min(start + recordsPerPage, allReservations.size());
			List<Reservation> reservations = allReservations.subList(start, end);
			int noOfPages = (int) Math.ceil(allReservations.size() * 1.0 / recordsPerPage);
			req.setAttribute("reservations", reservations);
			req.setAttribute("noOfPages", noOfPages);
			req.setAttribute("currentPage", page);
			req.setAttribute("searchTerm", searchTerm);
			req.setAttribute("sortBy", sortBy);
			req.setAttribute("sortOrder", sortOrder);
			RequestDispatcher rd = req.getRequestDispatcher("/jsp/list.jsp");
			rd.forward(req, resp);
		} else if ("edit".equals(action)) {
			int id = Integer.parseInt(req.getParameter("id"));
			Reservation reservation = reservationDAO.getReservationById(id);
			req.setAttribute("reservation", reservation);
			RequestDispatcher rd = req.getRequestDispatcher("/jsp/edit.jsp");
			rd.forward(req, resp);
		} else if ("export_csv".equals(action)) {
			exportCsv(req, resp);
		} else if ("clean_up".equals(action)) {
			reservationDAO.cleanUpPastReservations();
			req.setAttribute("successMessage", "過去の予約をクリーンアップしました。");
			resp.sendRedirect("reservation?action=list");
		} else {
			resp.sendRedirect("index.jsp");
		}
	}

	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		req.setCharacterEncoding("UTF-8");
		String action = req.getParameter("action");
		if ("add".equals(action)) {
			String name = req.getParameter("name");
			String reservationTimeString = req.getParameter("reservation_time");
			if (name == null || name.trim().isEmpty()) {
				req.setAttribute("errorMessage", "名前は必須です。");
				RequestDispatcher rd = req.getRequestDispatcher("/index.jsp");
				rd.forward(req, resp);
				return;
			}
			
			
			
			if (reservationTimeString == null || reservationTimeString.isEmpty()) {
				req.setAttribute("errorMessage", "希望日時は必須です。");
				RequestDispatcher rd = req.getRequestDispatcher("/index.jsp");
				rd.forward(req, resp);
				return;
			}
			try {
				LocalDateTime reservationTime = LocalDateTime.parse(reservationTimeString);
				if (reservationTime.isBefore(LocalDateTime.now())) {
					req.setAttribute("errorMessage", "過去の日時は選択できません。");
					RequestDispatcher rd = req.getRequestDispatcher("/index.jsp");
					rd.forward(req, resp);
					return;
				}
				if (!reservationDAO.addReservation(name, reservationTime)) {
					req.setAttribute("errorMessage", "同じ名前と日時での予約は既に存在します。");
					RequestDispatcher rd = req.getRequestDispatcher("/index.jsp");
					rd.forward(req, resp);
					return;
				}
				resp.sendRedirect("reservation?action=list");
			} catch (DateTimeParseException e) {
				req.setAttribute("errorMessage", "有効な日時を入力してください。");
				RequestDispatcher rd = req.getRequestDispatcher("/index.jsp");
				rd.forward(req, resp);
			}
		} else if ("update".equals(action)) {
			int id = Integer.parseInt(req.getParameter("id"));
			String name = req.getParameter("name");
			String reservationTimeString = req.getParameter("reservation_time");
			if (name == null || name.trim().isEmpty()) {
				req.setAttribute("errorMessage", "名前は必須です。");
				RequestDispatcher rd = req.getRequestDispatcher("/jsp/edit.jsp");
				rd.forward(req, resp);
				return;
			}
			if (reservationTimeString == null || reservationTimeString.trim().isEmpty()) {
				req.setAttribute("errorMessage", "希望日時は必須です。");
				RequestDispatcher rd = req.getRequestDispatcher("/jsp/edit.jsp");
				rd.forward(req, resp);
				return;
			}
			try {
				LocalDateTime reservationTime = LocalDateTime.parse(reservationTimeString);
				if (reservationTime.isBefore(LocalDateTime.now())) {
					req.setAttribute("errorMessage", "過去の日時は選択できません。");
					RequestDispatcher rd = req.getRequestDispatcher("/jsp/edit.jsp");
					rd.forward(req, resp);
					return;
				}
				if (!reservationDAO.updateReservation(id, name, reservationTime)) {
					req.setAttribute("errorMessage", "同じ名前と日時での予約は既に存在します。");
					RequestDispatcher rd = req.getRequestDispatcher("/jsp/edit.jsp");
					rd.forward(req, resp);
					return;
				}
				resp.sendRedirect("reservation?action=list");
			} catch (DateTimeParseException e) {
				req.setAttribute("errorMessage", "有効な日時を入力してください。");
				RequestDispatcher rd = req.getRequestDispatcher("/jsp/edit.jsp");
				rd.forward(req, resp);
			}
		} else if ("delete".equals(action)) {
			int id = Integer.parseInt(req.getParameter("id"));
			reservationDAO.deleteReservation(id);
			resp.sendRedirect("reservation?action=list");
		} else if ("import_csv".equals(action)) {
			try {
				Part filePart = req.getPart("csvFile");
				if (filePart != null && filePart.getSize() > 0) {
					try (BufferedReader reader = new BufferedReader(new InputStreamReader(filePart.getInputStream(),
							"UTF-8"))) {
						reservationDAO.importReservations(reader);
						req.setAttribute("successMessage", "CSV ファイルのインポートが完了しました。");
					}
				} else {
					req.setAttribute("errorMessage", "インポートするファイルを選択してください。");
				}
			} catch (Exception e) {
				req.setAttribute("errorMessage", "CSV ファイルのインポート中にエラーが発生しました: " +
						e.getMessage());
				e.printStackTrace();
			}
			RequestDispatcher rd = req.getRequestDispatcher("/jsp/list.jsp");
			rd.forward(req, resp);
		} else {
			resp.sendRedirect("index.jsp");
		}
	}

	private void exportCsv(HttpServletRequest req, HttpServletResponse resp) throws IOException {
		resp.setContentType("text/csv; charset=UTF-8");
		resp.setHeader("Content-Disposition", "attachment; filename=\"reservations.csv\"");
		PrintWriter writer = resp.getWriter();
		writer.append("ID,名前,予約日時\n");
		List<Reservation> records = reservationDAO.getAllReservations();
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm");
		for (Reservation record : records) {
			writer.append(String.format("%d,%s,%s\n",
					record.getId(),
					record.getName(),
					record.getReservationTime() != null ? record.getReservationTime().format(formatter) : ""));
		}
		writer.flush();
	}
}