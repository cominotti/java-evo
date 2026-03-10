package dev.cominotti.java.evo.jsonb;

import dev.cominotti.java.evo.Email;

public class EmailJsonbAdapter extends StringEvoJsonbAdapter<Email> {
    public EmailJsonbAdapter() { super(Email::value, Email.class); }
}
