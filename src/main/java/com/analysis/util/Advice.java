package com.analysis.util;

/**
 * one of: object Instantiation, method call or assert
 */
public enum Advice {
    OBJECTI, //"object instantiation"
    METHODI,
    ASSERT, Pass,
}
