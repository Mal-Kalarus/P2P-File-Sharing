package ServerApp;


/**
* ServerApp/ServerHelpHelper.java .
* Generated by the IDL-to-Java compiler (portable), version "3.2"
* from Server.idl
* Tuesday, August 27, 2019 1:30:42 o'clock PM EDT
*/

abstract public class ServerHelpHelper
{
  private static String  _id = "IDL:ServerApp/ServerHelp:1.0";

  public static void insert (org.omg.CORBA.Any a, ServerApp.ServerHelp that)
  {
    org.omg.CORBA.portable.OutputStream out = a.create_output_stream ();
    a.type (type ());
    write (out, that);
    a.read_value (out.create_input_stream (), type ());
  }

  public static ServerApp.ServerHelp extract (org.omg.CORBA.Any a)
  {
    return read (a.create_input_stream ());
  }

  private static org.omg.CORBA.TypeCode __typeCode = null;
  synchronized public static org.omg.CORBA.TypeCode type ()
  {
    if (__typeCode == null)
    {
      __typeCode = org.omg.CORBA.ORB.init ().create_interface_tc (ServerApp.ServerHelpHelper.id (), "ServerHelp");
    }
    return __typeCode;
  }

  public static String id ()
  {
    return _id;
  }

  public static ServerApp.ServerHelp read (org.omg.CORBA.portable.InputStream istream)
  {
    return narrow (istream.read_Object (_ServerHelpStub.class));
  }

  public static void write (org.omg.CORBA.portable.OutputStream ostream, ServerApp.ServerHelp value)
  {
    ostream.write_Object ((org.omg.CORBA.Object) value);
  }

  public static ServerApp.ServerHelp narrow (org.omg.CORBA.Object obj)
  {
    if (obj == null)
      return null;
    else if (obj instanceof ServerApp.ServerHelp)
      return (ServerApp.ServerHelp)obj;
    else if (!obj._is_a (id ()))
      throw new org.omg.CORBA.BAD_PARAM ();
    else
    {
      org.omg.CORBA.portable.Delegate delegate = ((org.omg.CORBA.portable.ObjectImpl)obj)._get_delegate ();
      ServerApp._ServerHelpStub stub = new ServerApp._ServerHelpStub ();
      stub._set_delegate(delegate);
      return stub;
    }
  }

  public static ServerApp.ServerHelp unchecked_narrow (org.omg.CORBA.Object obj)
  {
    if (obj == null)
      return null;
    else if (obj instanceof ServerApp.ServerHelp)
      return (ServerApp.ServerHelp)obj;
    else
    {
      org.omg.CORBA.portable.Delegate delegate = ((org.omg.CORBA.portable.ObjectImpl)obj)._get_delegate ();
      ServerApp._ServerHelpStub stub = new ServerApp._ServerHelpStub ();
      stub._set_delegate(delegate);
      return stub;
    }
  }

}