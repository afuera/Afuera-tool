# Motivation: 
To study if developers have handled the UE-API usages with try-catch.

# How to
We should reuse classes: CountStackTraces, but the List<Signaler> should be constructed so that the Signaler
is app developer methods directly invoked UE-API usages (in res/RQ2/ue/), and ThrowStmts are UE-API usages.
We should also continue to use the primary analyze() method.
But, we should replace isAPI() with checking if the method is a call back, i.e., the method name starts with "on".

We should also have a folder called res/RQ2/unhandled/, where each document are UE-API usage where not handled.
How to determine if UE-API usage is not handled? If the propagation hits any callback method, or "isAPI()";

Be very careful!!! about FilePath. do not override any file, especially the ones with ground truth.
Check all file path before running!!!!!!!!!


# Stats:
Use same BoxPlotExceptionType java file, but replace ue with unhandled, replace all with ue. 
Bascially, show a percentage of unhandled UE-API usage among all UE-API usage. 
I assume the percentage should be very high (like 80 percent up).