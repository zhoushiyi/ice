// **********************************************************************
//
// Copyright (c) 2001
// MutableRealms, Inc.
// Huntsville, AL, USA
//
// All Rights Reserved
//
// **********************************************************************

public class Client
{
    private static void
    menu()
    {
        System.out.println(
            "usage:\n" +
            "t: send greeting as twoway\n" +
            "o: send greeting as oneway\n" +
            "O: send greeting as batch oneway\n" +
            "d: send greeting as datagram\n" +
            "D: send greeting as batch datagram\n" +
            "f: flush all batch requests\n" +
            "T: set a timeout\n" +
            "S: switch secure mode on/off\n" +
            "s: shutdown server\n" +
            "x: exit\n" +
            "?: help\n");
    }

    private static int
    run(String[] args, Ice.Communicator communicator)
    {
        Ice.Properties properties = communicator.getProperties();
        final String refProperty = "Hello.Hello";
        String ref = properties.getProperty(refProperty);
        if (ref == null)
        {
            System.err.println("property `" + refProperty + "' not set");
            return 1;
        }

        Ice.ObjectPrx base = communicator.stringToProxy(ref);
        HelloPrx twoway = HelloPrx.checkedCast(base);
        if (twoway == null)
        {
            System.err.println("invalid object reference");
            return 1;
        }
        HelloPrx oneway = HelloPrx.uncheckedCast(twoway._oneway());
        HelloPrx batchOneway = HelloPrx.uncheckedCast(twoway._batchOneway());
        HelloPrx datagram = HelloPrx.uncheckedCast(twoway._datagram());
        HelloPrx batchDatagram =
            HelloPrx.uncheckedCast(twoway._batchDatagram());

        boolean secure = false;
        int timeout = -1;

        menu();

        java.io.BufferedReader in = null;
        try
        {
            in = new java.io.BufferedReader(
                new java.io.InputStreamReader(System.in));
        }
        catch(java.io.IOException ex)
        {
            ex.printStackTrace();
            return 1;
        }

        String line;
        do
        {
            try
            {
                System.out.print("==> ");
                System.out.flush();
                line = in.readLine();
                if (line == null)
                {
                    break;
                }
                if (line.equals("t"))
                {
                    twoway.hello();
                }
                else if (line.equals("o"))
                {
                    oneway.hello();
                }
                else if (line.equals("O"))
                {
                    batchOneway.hello();
                }
                else if (line.equals("d"))
                {
                    datagram.hello();
                }
                else if (line.equals("D"))
                {
                    batchDatagram.hello();
                }
                else if (line.equals("f"))
                {
                    batchOneway._flush();
                    batchDatagram._flush();
                }
                else if (line.equals("T"))
                {
                    if (timeout == -1)
                    {
                        timeout = 2000;
                    }
                    else
                    {
                        timeout = -1;
                    }

                    twoway = HelloPrx.uncheckedCast(twoway._timeout(timeout));
                    oneway = HelloPrx.uncheckedCast(oneway._timeout(timeout));
                    batchOneway =
                        HelloPrx.uncheckedCast(batchOneway._timeout(timeout));

                    if (timeout == -1)
                    {
                        System.out.println("timeout is now switched off");
                    }
                    else
                    {
                        System.out.println("timeout is now set to 2000ms");
                    }
                }
                else if (line.equals("S"))
                {
                    secure = !secure;

                    twoway = HelloPrx.uncheckedCast(twoway._secure(secure));
                    oneway = HelloPrx.uncheckedCast(oneway._secure(secure));
                    batchOneway =
                        HelloPrx.uncheckedCast(batchOneway._secure(secure));
                    datagram =
                        HelloPrx.uncheckedCast(datagram._secure(secure));
                    batchDatagram =
                        HelloPrx.uncheckedCast(batchDatagram._secure(secure));

                    if (secure)
                    {
                        System.out.println("secure mode is now on");
                    }
                    else
                    {
                        System.out.println("secure mode is now off");
                    }
                }
                else if (line.equals("s"))
                {
                    twoway.shutdown();
                }
                else if (line.equals("x"))
                {
                    // Nothing to do
                }
                else if (line.equals("?"))
                {
                    menu();
                }
                else
                {
                    System.out.println("unknown command `" + line + "'");
                    menu();
                }
            }
            catch(Ice.LocalException ex)
            {
                ex.printStackTrace();
            }
        }
        while (!line.equals("x"));

        return 0;
    }

    public static void
    main(String[] args)
    {
        int status = 0;
        Ice.Communicator communicator = null;

        try
        {
            Ice.Properties properties =
                Ice.Util.createPropertiesFromFile(args, "config");
            communicator = Ice.Util.initializeWithProperties(properties);
            status = run(args, communicator);
        }
        catch(Ice.LocalException ex)
        {
            ex.printStackTrace();
            status = 1;
        }

        if (communicator != null)
        {
            try
            {
                communicator.destroy();
            }
            catch(Ice.LocalException ex)
            {
                ex.printStackTrace();
                status = 1;
            }
        }

        System.exit(status);
    }
}
