Apache James IMAP default processing setup
==========================================

Prepares a minimal setup in which (some) IMAP client commands can be processed.
This can be used as a starting point to investigate what (programming) is needed 
to use the Apache James IMAP protocols package in a custom environment
(Maven dependency org.apache.james.protocols:protocols-imap:1.6.3).

In short, look at the "runClientCommands" method in class TestCommands:
http://./src/main/java/com/descartes/gos/jimap/TestCommands.java

Apache James IMAP website is at:
http://james.apache.org/protocols/imap4.html
