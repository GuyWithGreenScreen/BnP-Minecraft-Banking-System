package dev.gwgs.minecrafteconomy.networking;

public enum MCERequests {
    MINT("{\"request\": \"mint\", \"dat\": {\"amount\": 0, \"item_id\": \"\", \"item_name\": \"\"}}"),
    UNMINT("{\"request\": \"unmint\", \"dat\": {\"amount\": 0, \"resource_id\": \"\"}}"),
    DEPOSIT("{\"request\": \"deposit\", \"dat\": {\"currency\": 0, \"amount\": 0, \"currency_id\": \"\", \"user_id\": \"\"}}"),
    WITHDRAW("{\"request\": \"withdraw\", \"dat\": {\"currency\": 0, \"amount\": 0, \"currency_id\": \"\", \"user_id\": \"\", \"password\":\"\"}}"),
    CREATE_CHEQUE("{\"request\": \"create_cheque\", \"dat\": {\"currency\": 0, \"currency_id\": \"\", \"amount\": 0, \"from_user_id\": \"\", \"from_user_password\": \"\", \"to_user_id\": \"\"}}"),
    CREATE_INVOICE("{\"request\": \"create_invoice\", \"dat\": {\"currency\": 0, \"currency_id\": \"\", \"amount\": 0, \"from_user_id\": \"\", \"from_user_password\": \"\", \"recipient_user_id\": \"\"}}"),
    CASH_CHEQUE("{\"request\": \"cash_cheque\", \"dat\": {\"cheque_id\": \"\", \"user_id\": \"\", \"password\":\"\"}}"),
    PAY_INVOICE("{\"request\": \"pay_invoice\", \"dat\": {\"invoice_id\": \"\", \"user_id\": \"\", \"password\":\"\"}}");


    public final String requestTemplate;

    MCERequests(String requestTemplate) {
        this.requestTemplate = requestTemplate;
    }
}
