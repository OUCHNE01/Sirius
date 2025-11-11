package com.freelancing.utility;

public class Constants {

	public enum UserRole {
		ROLE_CUSTOMER("Customer"), ROLE_ADMIN("Admin"), ROLE_TECH_EXPERT("Tech Expert");

		private String role;

		private UserRole(String role) {
			this.role = role;
		}

		public String value() {
			return this.role;
		}
	}

	public enum ActiveStatus {
		ACTIVE("Active"), DEACTIVATED("Deactivated");

		private String status;

		private ActiveStatus(String status) {
			this.status = status;
		}

		public String value() {
			return this.status;
		}
	}

	public enum ServiceNegotiationStatus {
		PENDING("Pending"), APPROVED("Approved"), DENY("Deny"), CLOSE("Close");

		private String status;

		private ServiceNegotiationStatus(String status) {
			this.status = status;
		}

		public String value() {
			return this.status;
		}
	}

	public enum ServiceRequestStatus {
		PENDING("Pending"), APPROVED("Approved"), WORKING("Working"), DONE_AND_CLOSE("Done & Close"), CLOSE("Close");

		private String status;

		private ServiceRequestStatus(String status) {
			this.status = status;
		}

		public String value() {
			return this.status;
		}
	}

	public enum PaymentGatewayTxnType {
		CREATE_ORDER("Create Order"), PAYMENT("Payment");

		private String type;

		private PaymentGatewayTxnType(String type) {
			this.type = type;
		}

		public String value() {
			return this.type;
		}
	}

	public enum PaymentGatewayTxnStatus {
		SUCCESS("Success"), FAILED("Failed");

		private String type;

		private PaymentGatewayTxnStatus(String type) {
			this.type = type;
		}

		public String value() {
			return this.type;
		}
	}

}
