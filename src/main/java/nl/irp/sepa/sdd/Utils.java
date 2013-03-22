package nl.irp.sepa.sdd;

import static com.google.common.base.Preconditions.checkArgument;

import iso.std.iso._20022.tech.xsd.pain_008_001.AccountIdentification4Choice;
import iso.std.iso._20022.tech.xsd.pain_008_001.ActiveOrHistoricCurrencyAndAmount;
import iso.std.iso._20022.tech.xsd.pain_008_001.BranchAndFinancialInstitutionIdentification4;
import iso.std.iso._20022.tech.xsd.pain_008_001.CashAccount16;
import iso.std.iso._20022.tech.xsd.pain_008_001.CreditorReferenceInformation2;
import iso.std.iso._20022.tech.xsd.pain_008_001.CreditorReferenceType1Choice;
import iso.std.iso._20022.tech.xsd.pain_008_001.CreditorReferenceType2;
import iso.std.iso._20022.tech.xsd.pain_008_001.DocumentType3Code;
import iso.std.iso._20022.tech.xsd.pain_008_001.FinancialInstitutionIdentification7;
import iso.std.iso._20022.tech.xsd.pain_008_001.PartyIdentification32;
import iso.std.iso._20022.tech.xsd.pain_008_001.PaymentIdentification1;
import iso.std.iso._20022.tech.xsd.pain_008_001.RemittanceInformation5;
import iso.std.iso._20022.tech.xsd.pain_008_001.StructuredRemittanceInformation7;

import java.math.BigDecimal;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.regex.Pattern;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeConstants;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;

public class Utils {
	
	
	private static Pattern bicRegex =
			Pattern.compile("([a-zA-Z]{4}[a-zA-Z]{2}[a-zA-Z0-9]{2}([a-zA-Z0-9]{3})?)");

	public static XMLGregorianCalendar createXMLGregorianCalendar(Date currentDateTime) {
		GregorianCalendar calendar = new GregorianCalendar();
		calendar.setTime(currentDateTime);

		XMLGregorianCalendar createDate;
		try {
			createDate = DatatypeFactory.newInstance().newXMLGregorianCalendar(calendar);
			createDate.setMillisecond(DatatypeConstants.FIELD_UNDEFINED);
			createDate.setTimezone(DatatypeConstants.FIELD_UNDEFINED);
		} catch (DatatypeConfigurationException e) {
			throw new RuntimeException(e);
		}

		return createDate;
	}
	
	public static XMLGregorianCalendar createXMLGregorianCalendarDate(Date currentDateTime) {
		GregorianCalendar calendar = new GregorianCalendar();
		calendar.setTime(currentDateTime);

		XMLGregorianCalendar createDate;
		try {
			createDate = DatatypeFactory.newInstance().newXMLGregorianCalendarDate(
				calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH)+1, calendar.get(Calendar.DAY_OF_MONTH),
				DatatypeConstants.FIELD_UNDEFINED);
		} catch (DatatypeConfigurationException e) {
			throw new RuntimeException(e);
		}

		return createDate;
	}
	
	/**
	 * Information supplied to enable the matching of an entry with the items that the
	 * transfer is intended to settle, such as commercial invoices in an accounts' receivable
	 * system
	 * max length: 140
	 * @return
	 */
	public static RemittanceInformation5 createRmtInf(String info) {
		checkMax140Text(info);
		
		RemittanceInformation5 remittanceInformation = new RemittanceInformation5();
		remittanceInformation.getUstrd().add(info);
		return remittanceInformation;
	}
	
	/**
	 * Information supplied to enable the matching of an entry with the items that the
	 * transfer is intended to settle, such as commercial invoices in an accounts' receivable
	 * system
	 * max length: 140
	 * @return
	 */
	public static RemittanceInformation5 createRmtInf_struct(String ref) {
		checkMax35Text(ref);
		
		RemittanceInformation5 remittanceInformation = new RemittanceInformation5();
		StructuredRemittanceInformation7 structuredRemittanceInformation = new StructuredRemittanceInformation7();
		CreditorReferenceInformation2 creditorReferenceInformation = new CreditorReferenceInformation2();
		structuredRemittanceInformation.setCdtrRefInf(creditorReferenceInformation);
		
		// Only 'SCOR' is allowed.
		CreditorReferenceType2 creditorReferenceType = new CreditorReferenceType2();
		CreditorReferenceType1Choice creditorReferenceType1Choice = new CreditorReferenceType1Choice();	
		creditorReferenceType1Choice.setCd(DocumentType3Code.SCOR);
		creditorReferenceType.setCdOrPrtry(creditorReferenceType1Choice);
		creditorReferenceInformation.setTp(creditorReferenceType);
		
		creditorReferenceInformation.setRef(ref);
		
		remittanceInformation.getStrd().add(structuredRemittanceInformation);
		return remittanceInformation;
	}
	
	/**
	 * Unambiguous identification of a account
	 * @return 
	 */
	public static CashAccount16 createAccount(String iban) {
		//sepacheckArgument(ibanRegex.matcher(iban).matches(), "This doesn't look like a correct IBAN id '"+iban+"'");
		
		CashAccount16 account = new CashAccount16();
		AccountIdentification4Choice creditorAccountId = new AccountIdentification4Choice();
		// Only IBAN is allowed.
		creditorAccountId.setIBAN(iban);
		account.setId(creditorAccountId);
		return account;
	}
	
	public static PartyIdentification32 createParty(String nm) {
		checkMax70Text(nm);
		
		PartyIdentification32 party = new PartyIdentification32();
		party.setNm(nm);
		return party;
	}
	
	/**
	 * @param instructionIdentification Unique identification as assigned by an instructing party for an instructed party to 
	 * unambiguously identify the instruction.
	 * @param endToEndIdentification Unique identification assigned by the initiating party to unumbiguously identify the
	 * transaction. This identification is passed on, unchanged, throughout the entire end-
	 * to-end chain.
	 * @return
	 */
	public static PaymentIdentification1 createPaymentIdentification(String instructionIdentification, String endToEndIdentification) {
		PaymentIdentification1 paymentIdentification = new PaymentIdentification1();
		 
		checkMax35Text(instructionIdentification);
		paymentIdentification.setInstrId(instructionIdentification);

		checkMax35Text(endToEndIdentification);
		paymentIdentification.setEndToEndId(endToEndIdentification);
		
		return paymentIdentification;
	}
	
	public static BranchAndFinancialInstitutionIdentification4 createFinInstnId(String bic) {
		checkArgument(bicRegex.matcher(bic).matches(), "This doesn't look like a correct BIC id '"+bic+"'");
		
		BranchAndFinancialInstitutionIdentification4 creditorAgent = new BranchAndFinancialInstitutionIdentification4();
		FinancialInstitutionIdentification7 creditorfinancialInstitutionIdentification = new FinancialInstitutionIdentification7();
		// Only BIC is allowed.
		creditorfinancialInstitutionIdentification.setBIC(bic);
		creditorAgent.setFinInstnId(creditorfinancialInstitutionIdentification);
		return creditorAgent;
	}
	
	public static ActiveOrHistoricCurrencyAndAmount createAmount(BigDecimal amount) {
		ActiveOrHistoricCurrencyAndAmount instdAmt = new ActiveOrHistoricCurrencyAndAmount();
		instdAmt.setValue(amount);
		instdAmt.setCcy("EUR");
		return instdAmt;
	}
	
	private static void checkMax35Text(String text) {
		checkArgument(text.length()<=35, "length of field is more than 35");
		checkArgument(text.length()>=1, "length of field is less than 1");
	}
	
	private static void checkMax70Text(String text) {
		checkArgument(text.length()<=70, "length of field is more than 70");
		checkArgument(text.length()>=1, "length of field is less than 1");
	}
	
	private static void checkMax140Text(String text) {
		checkArgument(text.length()<=140, "length of field is more than 140");
		checkArgument(text.length()>=1, "length of field is less than 1");
	}
	
}
