// **********************************************************************
//
// Copyright (c) 2001
// MutableRealms, Inc.
// Huntsville, AL, USA
//
// All Rights Reserved
//
// **********************************************************************

#include <Ice/Incoming.h>
#include <Ice/ObjectAdapter.h>
#include <Ice/Object.h>
#include <Ice/LocalException.h>

using namespace std;
using namespace Ice;
using namespace IceInternal;

IceInternal::Incoming::Incoming(const InstancePtr& instance, const ObjectAdapterPtr& adapter) :
    _adapter(adapter),
    _is(instance),
    _os(instance)
{
}

IceInternal::Incoming::~Incoming()
{
}

void
IceInternal::Incoming::invoke(Stream& is)
{
    _is.swap(is);
    string identity;
    _is.read(identity);
    string operation;
    _is.read(operation);

    Stream::Container::size_type statusPos = _os.b.size();

    ObjectPtr servant = _adapter->identityToServant(identity);
    ServantLocatorPtr locator;
    LocalObjectPtr cookie;

    try
    {
	if (!servant)
	{
	    locator = _adapter->getServantLocator();
	    if (locator)
	    {
		servant = locator->locate(_adapter, identity, operation, cookie);
	    }
	}
	
	if(!servant)
	{
	    _os.write(static_cast<Byte>(DispatchObjectNotExist));
	}
	else
	{
	    _os.write(static_cast<Byte>(DispatchOK));
	    DispatchStatus status = servant->__dispatch(*this, operation);
	    *(_os.b.begin() + statusPos) = static_cast<Byte>(status);
	}

	if (locator && servant)
	{
	    locator->finished(_adapter, identity, servant, operation, cookie);
	}
    }
    catch(const LocationForward& p)
    {
	if (locator && servant)
	{
	    locator->finished(_adapter, identity, servant, operation, cookie);
	}
	_os.b.resize(statusPos);
	_os.write(static_cast<Byte>(DispatchLocationForward));
	_os.write(p._prx);
	return;
    }
    catch(const LocalException&)
    {
	if (locator && servant)
	{
	    locator->finished(_adapter, identity, servant, operation, cookie);
	}
	_os.b.resize(statusPos);
	_os.write(static_cast<Byte>(DispatchLocalException));
	throw;
    }
    catch(...)
    {
	if (locator && servant)
	{
	    locator->finished(_adapter, identity, servant, operation, cookie);
	}
	_os.b.resize(statusPos);
	_os.write(static_cast<Byte>(DispatchUnknownException));
	throw UnknownException(__FILE__, __LINE__);
    }
}

Stream*
IceInternal::Incoming::is()
{
    return &_is;
}

Stream*
IceInternal::Incoming::os()
{
    return &_os;
}
