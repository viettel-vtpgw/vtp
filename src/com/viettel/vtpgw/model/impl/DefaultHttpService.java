package com.viettel.vtpgw.model.impl;

import java.util.regex.Pattern;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.viettel.vtpgw.model.HttpService;

public class DefaultHttpService extends DefaultService implements HttpService {
	private static final Logger LOG = LogManager.getLogger(DefaultHttpService.class);
  private static class Checker {
    boolean check(String params) {
      return true;
    }
  }

  private final class RegexChecker extends Checker {
    private final Pattern pattern;

    RegexChecker(Pattern pattern) {
      this.pattern = pattern;
    }

    @Override
    boolean check(String params) {
      return pattern.matcher(params).find();
    }
  }

  private final class SimpleChecker extends Checker {
    final String pattern;

    SimpleChecker(String pattern) {
      this.pattern = pattern;
    }

    @Override
    boolean check(String params) {
      return params.contains(pattern);
    }
  }

  boolean check;
  Checker checker;

  @Override
  public boolean check() {
    return check;
  }

  @Override
  public boolean check(String buff) {
    return !check || checker.check(buff);
  }

  public void setCheck(String check) {
    if (check.startsWith("/") && check.endsWith("/")) {
      try {
        Pattern pattern = Pattern.compile(check.substring(1, check.length() - 1));
        this.checker = new RegexChecker(pattern);
        this.check = true;
      } catch (Exception e) {
      	LOG.debug(e);
      }
    } else {
      this.checker = new SimpleChecker(check);
      this.check = true;
    }
  }
}
