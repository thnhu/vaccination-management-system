package Vaccination.Management.System.advisor.service;

public interface FaqService {
    /** Returns a pre-built answer if the message matches a known FAQ, otherwise null. */
    String lookup(String userMessage);
}
