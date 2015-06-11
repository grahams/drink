.pl 10.0i
.po 0
.ll 7.2i
.lt 7.2i
.nr LL 7.2i
.nr LT 7.2i
.ds LF Hunt
.ds RF [Page %]FORMFEED
.ds CF
.ds LH RFC XXXX
.ds RH March 1996
.ds CH Vending Machine Transaction Protocol
.hy 0
.ad l
.in 0
Network Working Group                                            T. Hunt
Request for Comments: XXXX                        Computer Science House
Category: Experimental                                        March 1996


.ce
VMTP -- Vending Machine Transaction Protocol

.ti 0
Status of this Memo

.fi
.in 3
This memo specifies an Experimental Vending Machine Protocol for the
Internet community.  This memo does not specify an Internet standard 
of any kind.  Discussion and suggestions for improvement are requested.
Distribution of this memo is unlimited.

.ti 0
Abstract

Imagine a world, a world where one can order a drink through his computer
from the vending machine around the corner.  A world where one can decide
which machine has the best selection before leaving the comfort of his
office.  All this -- and more -- will be made possible with VMTP;  The Vending
Machine Transaction Protocol.

Of the many sundry protocols utilizied in the internet infrastructure,
none attempt to address the issues involved with connectivity of vending
machines.  Well, the time has come.  This RFC defines the specification
for a vending machine transaction protocol.  Throughout this document,
the terms "drop" and "droping" refer to purchasing a product from a vending
machine. (Most vending machines drop their product when you purchase it).

The protocol provides for a sufficient range of commands to enable users
to be authenticated and perform all basic vending needs, from determining
information about the stock and status of all connected machines, to
purchasing products on credit or debit, to transfering balances from
their account to other accounts, and finally to determine their usage
statistics.  Similarly, the protocol specifies commands for administrators
to update balances, change the inventory of any attached machines, and
modify user statistics.  All essential vending transaction operations are
covered.

Additionally, the protocol specifies some commands and events above the basic
vending needs.  The three such commands "drop" a random product from a
selected machine, gamble on either a random or specific product from a
machine, and send a message to all connected users, respectively.  The event
is optional notification when and what product another user has "dropped".

This is the niche that VMTP fills.  Any questions, comments, and
reports of defects or ambiguities in this specification may be sent
to the CSH drink mailing list, using the address <drink@csh.rit.edu>.

.ti 0
1. Introduction

The VMTP protocol specifies a client server communication protocol similar
in style to SMTP[1], NNTP[2] and many other Internet protocols currently in
use.  It is sequence of commands, replies, and events.  The niche that it
fills is one that is sure to grow in the not too distant future.  Much
thought and some humor have gone into the design of the protocol.

A prototype of this vending protocol protocol has been in operation at
Computer Science House (CSH) for about a year, and the protocol described
herein is currently in use for our two drink machines.  This RFC is the
result of maturing the prototype into a final form, and was used prior
to submission as the implemntation specification.

.ti 0
2. The VMTP Protocol

The VMTP protocol is based on a request/response mechanism.  Requests
are sent from clients to the vend server (the vendor), and the server
responds in a predetermined manner.  Additionally, there are two events that
the server sends to all connected clients.  Drop messages and broadcast
messages.  This results in two languages,  the client --> server language
and the server --> client language, each with different syntax.  All
client requests begin with a variable length command word in upper or
lowercase, followed by whitespace separated arguments, followed by a newline.
Server responses and events come in two forms: single line and multiline.
All responses and events begin with a 3 digit ascii number, defined below,
followed by a single space, followed by the response or event data.
Multiline responses are similar, but each line in the multiline response
except for the last line follows the number by a dash, rather than a space.
The last line follows the number with a space to inform the client that this
is the last line of the response.

.ti 0
2.1 The Client --> VMTP Server Protocol Syntax (in BNF)

	<Request>		::= <Cmd> <EOL> | <Request> <Cmd>
	<Cmd>			::= <AdminCmd> | <UserCmd> |
				    <HelpCmd>

	<HelpCmd>		::= "HELP" | "HELP" <WS> <STRING>

	<UserCmd>		::= <UserAuthCmd> |
				    <GetMachineInfo> |
				    <Search> | <SelectMachine> |
				    <GetUserInfo> |
				    <DropIt> | <ListMachineInfo>

	<DropIt>		::= "DROP" <WS> <SlotNum>
				    <WS> <Delay>
	<SelectMachine>		::= "MACH" <WS> <MachineName>
	<UserAuthCmd>		::= "AUTH" <WS> <AuthType> <WS>
				    <AuthInfo>
	<AuthType>		::= "PW" | "KERB" | ...
	<AuthInfo>		::= <User> <WS> <Password> |
				    <KerberosAuthInfo> | ...
	<GetMachineInfo>	::= "MINFO" <WS> <MachineInfo>
	<ListMachineInfo>	::= "LSTMACH" <WS> <MachineInfo>
	<Search>		::= "SEARCH" <STRING>

	<MachineName>		::= <InfoValue>
	<MachineInfo>		::= <Identifier> |
				    <Identifier> <WS>
				    <MachineInfo>

	<Delay>			::= <NUM>

	<AdminCmd>		::= <AdminAuthCmd> |
				    <AdminUnAuthCmd> |
				    <UserAdminCmd> |
				    <MachineAdminCmd>
	<UserAdminCmd>		::= <ChangeBalance> |
				    <SetUserInfo> |
				    <TransferBalance> |
				    <RemoveUserInfo> |
				    <DeleteUser> |
				    <ListUserInfo>

	<ChangeBalance>		::= <SetBalance> |
				    <UpdateBalance>
	<MachineAdminCmd>	::= <UpdateInventory> |
				    <UpdateStockInfo> |
				    <UpdateMachineInfo>
	<UpdateInventory>	::= <SetSlotCount> |
				    <UpdateSlotCount> 
	<UpdateStockInfo>	::= <SetSlotName> |
				    <SetOptionalSlotInfo> |
				    <SetSlotCost> |
				    <DeleteSlot> | 
				    <RemoveSlotInfo>
	
	<UpdateMachineInfo>	::= "SMINFO" <WS> <MachineInfo>
				    <WS> <InfoValue>
	<GetUserInfo>		::= "UINFO" | "UINFO" <WS>
				    <UserInfo>
	<SetUserInfo>		::= "SUINFO" | "SUINFO" <WS>
				    <User> <WS> <UserInfo>
	<ListUserInfo>		::= "LUINFO" | "LUINFO" <UserInfo>
	<AdminAuthCmd>		::= "ADMIN" <WS> <User>
				    <WS> <Password>
	<AdminUnAuthCmd>	::= "UNADMIN"
	<DeleteUser>		::= "DELUSR" <WS> <User>
	<RemoveUserInfo>	::= "DELUSRINF" <WS> <User>
				    <WS> <Identifier>
	<TransferBalance>	::= "TRANS" <WS> <User> <WS> <User>
	<SetUserInfo>		::= "USRINF" <WS> <User> <WS>
				    <Identifier> <WS> <InfoValue>
	<SetBalance>		::= "SUINFO" <WS> <User> <WS>
				    "Balance" <WS> <NUM>
	<UpdateBalance>		::= "SUINFO" <WS> <User> <WS>
				    "Balance" <WS> <Count>
	<SetSlotName>		::= "SMINFO" <WS> <Machine> <WS>
				    "Balance" <WS> <Count>
	<SetSlotName>		::= "SLTNAME" <WS> <SlotName>
	<SetOptionalSlotInfo>	::= "SLTINFO" <WS> <Identifier>
				    <WS> <InfoValue>
	<SetSlotCost>		::= "SLTCOST" <WS> <SlotNum>
				    <Cents>
	<DeleteSlot>		::= "DELSLT" <WS> <SlotNum>
	<RemoveSlotInfo>	::= "DELSLTINF" <WS> <SlotNum>
				    <WS> <Identifier>
	<SetSlotCount>		::= "SLTCNT" <WS> <SlotNum>
				    <WS> <NUM>
	<UpdateSlotCount>	::= "UPDSLTCNT" <WS> <SlotNum>
				    <WS> <Count>

	<UserInfo>		::= <UserInfoType> | <UserInfoType>
				    <WS> <UserInfo>

	<UserInfoType>		::= "BAL" | "NAME" | "STATS"
	<InfoName>		::= <Identifier>
	<InfoValue>		::= "file:" <FileName> |
				    "exec:" <ExecCmd> |
				    <STRING>
	<password>		::= <STRING>
	<User>			::= <Identifier>
	<Cents>			::= <NUM>
	<FileName>		::= <STRING>
	<ExecCmd>		::= <STRING>
	<SlotName>		::= <STRING>
	<SlotNum>		::= <NUM>
	<Count>			::= <NEGNUM> | <NUM>
	<Identifier>		::= [a-z][A-Z][0-9]_ |
				    [a-z][A-Z][0-9]_ <Identifier>

	<STRING>		::= [\\000 - \\777] |
				    [\\000 - \\777] <STRING>
	<NUM>			::= <DIGIT> | <DIGIT> <Num>
	<NEGNUM>		::= "-" <NUM>
	<DIGIT>			::= 0 | 1 | 2 | 3 | 4 | 5 |
				    6 | 7 | 8 | 9
	<WS>			::= <SPACE> | <TAB> | <SPACE> <WS> |
				    <TAB> <WS>
	<SPACE>			::= "\\040"
	<TAB>			::= "\\t"
	<EOL>			::= "\\r\\n"

.ti 0
2.2 VMTP Protocol Overview

This section provides explainations for each VMTP client command.
The explainations include descriptions of the arguments and their meanings
and the syntax and examples of server responses.  Note that VMTP protocol
commands are case insensitive (ie: "balance", "Balance", "BALancE", etc
are all identical.)

There are two type of server responses and events: single line and multiple
line.  Every line of the response begins with a three character ASCII number.
Each digit in the number is significant and specifies more detail as
examined from left to right.  The BNF for single line responses is:

	<Response> ::= <DIGIT> <DIGIT> <DIGIT> <SPACE> <TEXT> <EOL>
	<TEXT>	   ::= <{<STRING> - <EOL>}>

and the BNF for multiline responses is:

	<MultiLineResponse> ::= <IntermediateLines> <LastLine>
.br
	<IntermediateLines> ::= <DashLine> |
				<DashLine> <IntermediateLines>
.br
	<DashLine>	    :== <DIGIT> <DIGIT> <DIGIT> '-'
				<TEXT> <EOL>
.br
	<LastLine> 	    ::= <DIGIT> <DIGIT> <DIGIT> <SPACE>
				<TEXT> <EOL>
.br


.ti 0
2.2 VMTP Server Response Code Overview

The following information is based closely on appendix E of the
SMTP[1] RFC, with extensions to allow for the VMTP server to send events
to clients.

There are nine values for the first digit of the reply code:

.ti 3
1yz   Positive Preliminary reply

The command was accepted but will not be executed until recipt of
confirmation of the information in this reply.  The client should send
another command specifying whether to continue or abort the action.

.ti 3 
2yz   Positive Completion reply

The request was completed successfully.  A new request may be initiated.

.ti 3
3yz   Positive intermediate reply

The command has been accepted, but the requested action is being
held in abeyance, pending recipt of further information.  The
client should send another command specifying this information.  This
reply is used in command sequence groups.

.ti 3
4yz   Transient Negative Completion reply

The  command was not accepted, and the requested action did not occur.
However, the error condition is temporary and the action may be requested
again.  The sender should return to the beginning of the command sequence
(if any).

.ti 3
5yz   Permanent Negative Completion reply

The command was not accepted and the requested action did not occur.  The
client is discouraged from repeating the exact request (in the same
sequence).

.ti 3
6yz   VMTP Server event

Events are sent from the server to the client without first receiving a
request from the client.  The only time the VMTP server may send events
is while it is not processing a client command.  It may not send
events while replying to a client command, only after that command is
completed.  Note that it is possible for the server to send an event while
the client is composing it's request, so effectively the event can come
before or after the server response, but not during.

.ti 3
7yz - 8yz   Reserved for future use

.ti 3
9yz   Debug reply

This reply code preceeds implementation dependent debugging information.

.ti 3
The second digit encodes responses in specific categories:

.ti 3
x0z   Syntax

These replies refer to syntax errors, syntatically correct commands that
don't fit any functional category, and unimplemented or superfluous
commands.

.ti 3
x1z   Information

These are replies to requests for information, such 
the various status requests, and the help request [see 2.3.25].

.ti 3
x2z   Connections

These are replies referring to the transmission channel.

.ti 3
x3z   Authentication

These are replies referring to authentication.

.ti 3
x4z   Vending Machine

These are replies referring to vending machines.

.ti 3
x5z   Vending system

These replies indicate successful completion of commands sent to the
VMTP server.

.ti 3
x6z   User

These are replies referring to users.

.ti 0
2.3 Detail of VMTP Commands, Events, and Responses

This section provides detailed explainations of each VMTP command
in the VMTP protocol including purpose, syntax, server response,
and recommended response messages.  Additionally, an example follows each
description showing a successful completion and a failure.

Any BNF variables in the command detail below not shown are assumed to
be taken from the VMTP client --> server syntax in section 2.1 with the
addition of these few:

	<OKREPLY> ::= "250" <SPACE> "Cha-Ching!" <EOL>
.br
	<TEXT>	   ::= <{<STRING> - <EOL>}>
.br
	<Failure>	 ::= <FailureCode> <SPACE> <TEXT> <EOL>
.br
	<FailureCode>	 ::= <DIGIT> <DIGIT> <DIGIT>

.ti 0
2.3.1 <DropIt> ::= "DROP" <WS> <SlotNum> <WS> <Delay>

This command is used to purchase a product from the currently selected
vending machine.  The two arguments are the slot number of the product
to purchase, and the number of seconds to delay before purchasing it.
The delay is to allow the user time to reach the vending machine before
it drops the purchased product so they don't have to worry about someone
stealing the product. 

.ti 3
Response

	<DropResponse>   ::= <OKREPLY> | <Failure>

This is a single line response.  The command does not complete until
after the delay expires and the product is dropped.  A successful response
means that the product was purchased, and the users balance will be
updated accordingly.  If the command fails, the users balance will not be
affected, and the message following the failure code will contain an
explaination of the type of error that occurred.

.ti 3
Failure Codes and Messages

	501 Syntax: Wrong number of arguments.
.br
	502 Syntax: Wrong type of arguments.
.br
	531 Auth: Client not authenticated.
.br
	541 Machine: Client has not selected a vending machine.
.br
	542 Machine: Delay out of range.
.br
	543 Machine: Slot number out of range.
.br
	549 Machine: "<implementation dependent failure message>".

.ti 3
Example

	Request:		"DROP 1 5"
.br
	Success Response:	"250 Cha-Ching!"
.br
	Failure Response:	"531 Auth: Client not authenticated."

.ti 0
2.3.2 <GetCost> ::= "COST" <WS> <SlotNum>

This command returns the cost of the specified slot in the currently
selected vending machine.  A successful response returns the cost of the
slot in as an integer number of cents.

.ti 3
Response

	<CostResponse> ::= <Cost> | <Failure>
	<Cost>	       ::= "210" <SPACE> <NUM> <EOL>

.ti 3
Failure Codes and Messages

	501 Syntax: Wrong number of arguments.
.br
	502 Syntax: Wrong type of arguments.
.br
	541 Machine: User has not selected a vending machine.
.br
	543 Machine: Slot number out of range.
.br
	549 Machine: "<implementation dependent failure message>".

.ti 3
Example

	Request:		"COST 1"
.br
	Success Response: 	"210 50"
.br
	Failure Response:	"501 Syntax: Wrong number of arguments"

.ti 0
2.3.3 <GetBalance> ::= "BALANCE"

This command is provided to enable the user to determine his balance.
The response contains the balance in cents.

.ti 3
Response:
	<BalanceResponse> ::= "211" <SPACE> <NUM>

.ti 3
Failure Codes and Messages

	501 Syntax: Wrong number of arguments.
.br
	531 Auth: Client not authenticated.
.br
	561 User: "<implementation dependent failure message>".

.ti 3
Example
	Request:		"BALANCE"
	Success Response:	"211 6969"
	FailureResponse:	"531 Auth: Client not authenticated."

.ti 0
2.3.4 <SelectMachine> ::= "MACH" <WS> <MachineName>

Before using any command that assumes that a machine has already
been selected, the "MACH" command must be used to select the machine.

.ti 3
Response:
	<SelectResponse> ::= <OKREPLY> | <Failure>

.ti 3
Failure Codes and Messages

	501 Syntax: Wrong number of arguments.
.br
	544 Machine: Unknown machine.
.br
	549 Machine: "<implementation dependent failure message>".
	

.ti 0
2.3.5 <UserAuthCmd> ::= "AUTH" <WS> <User> <WS> <Password>

This command is used to authenticate the user.  I think I will change it
to be able to use different authentication styles.

.ti 3
Response:

.ti 3
Failure Codes and Messages

.ti 3
Example

.ti 0
2.3.6 <GetMachineInfo> ::= "MACHINF" <WS> <MachineInfo>

This command is used to get information from the currently selected machine.

.ti 3
Response:

.ti 3
Failure Codes and Messages

.ti 3
Example

.ti 0
2.3.7 <ListMachineInfo> ::= "LSTMACH" <WS> <MachineInfo>

This command is used to get information from all machines.

.ti 3
Response:

.ti 3
Failure Codes and Messages

.ti 3
Example

.ti 0
2.3.8 <Search> ::= "SEARCH" <STRING>

I think I'm going to delete this command.

.ti 3
Response:

.ti 3
Failure Codes and Messages

.ti 3
Example

.ti 0
2.3.9 <UpdateMachineInfo> ::= "SETMACH" <WS> <MachineInfo>
			    <WS> <InfoValue>

This is an admin command, used to update information about the currently
selected machine.

.ti 3
Response:

.ti 3
Failure Codes and Messages

.ti 3
Example

.ti 0
2.3.10 <ListUserInfo> ::= "LUSRINF" | "LUSRINF" <UserInfo>

this is an admin command used to list information about all users.

.ti 3
Response:

.ti 3
Failure Codes and Messages

.ti 3
Example

.ti 0
2.3.11 <AdminAuthCmd> ::= "ADMIN" <WS> <User> <WS> <Password>

This command is used to authenticate

.ti 3
Response:

.ti 3
Failure Codes and Messages

.ti 3
Example

.ti 0
2.3.12 <AdminUnAuthCmd>	::= "UNADMIN"

This admin command is used to stop being an admin.

.ti 3
Response:

.ti 3
Failure Codes and Messages

.ti 3
Example

.ti 0
2.3.13 <DeleteUser> ::= "DELUSR" <WS> <User>

this admin command is used to delete a user

.ti 3
Response:

.ti 3
Failure Codes and Messages

.ti 3
Example

.ti 0
2.3.14 <RemoveUserInfo> ::= "DELUSRINF" <WS> <User> <WS> <Identifier>

This admin command is used to delete a particular piece of information
about a user.

.ti 3
Response:

.ti 3
Failure Codes and Messages

.ti 3
Example

.ti 0
2.3.15 <TransferBalance> ::= "TRANS" <WS> <User> <WS> <User>

This admin command is used to transfer a balance from one user to another

.ti 3
Response:

.ti 3
Failure Codes and Messages

.ti 3
Example

.ti 0
2.3.16 <SetUserInfo> ::= "USRINF" <WS> <User> <WS> <Identifier> <WS>
			 <InfoValue>

This command is used to set the value of a particular piece of user
information.

.ti 3
Response:

.ti 3
Failure Codes and Messages

.ti 3
Example

.ti 0
2.3.17 <SetBalance> ::= "SETBAL" <WS> <User> <WS> <Balance>

This admin command is used to set a users balance

.ti 3
Response:

.ti 3
Failure Codes and Messages

.ti 3
Example

.ti 0
2.3.18 <UpdateBalance> ::= "UPDBAL" <WS> <User> <WS> <Count>

This admin command is used to change a users balance

.ti 3
Response:

.ti 3
Failure Codes and Messages

.ti 3
Example

.ti 0
2.3.19 <SetSlotName> ::= "SLTNAME" <WS> <SlotName>

This admin command is used to set the name of a slot.

.ti 3
Response:

.ti 3
Failure Codes and Messages

.ti 3
Example

.ti 0
2.3.20 <SetOptionalSlotInfo> ::= "SLTINFO" <WS> <Identifier>
			       <WS> <InfoValue>

This admin command is used to set the value of a particular piece of
machine information.

.ti 3
Response:

.ti 3
Failure Codes and Messages

.ti 3
Example

.ti 0
2.3.21 <SetSlotCost> ::= "SLTCOST" <WS> <SlotNum> <WS> <Cents>

This admin command is used to set the cost of a slot.

.ti 3
Response:

.ti 3
Failure Codes and Messages

.ti 3
Example

.ti 0
2.3.22 <DeleteSlot> ::= "DELSLT" <WS> <SlotNum>

This admin command is used to delete a slot.

.ti 3
Response:

.ti 3
Failure Codes and Messages

.ti 3
Example

.ti 0
2.3.23 <RemoveSlotInfo> ::= "DELSLTINF" <WS> <SlotNum> <WS> <Identifier>

This admin command is used to delete a particular piece of information
about a slot.

.ti 3
Response:

.ti 3
Failure Codes and Messages

.ti 3
Example

.ti 0
2.3.24 <SetSlotCount> ::= "SLTCNT" <WS> <SlotNum> <WS> <NUM>

This admin command is used to set the number of products a particular
slot.

.ti 3
Response:

.ti 3
Failure Codes and Messages

.ti 3
Example

.ti 0
2.3.25 <UpdateSlotCount> ::= "UPDSLTCNT" <WS> <SlotNum> <WS> <Count>

This admin command is used to change the number of products a particular
slot.

.ti 3
Response:

.ti 3
Failure Codes and Messages

.ti 3
Example

.ti 0
2.3.26 <HelpCmd> ::= "HELP" | "HELP" <WS> <STRING>

This command shows a brief help message.

.ti 3
Response:

.ti 3
Failure Codes and Messages

.ti 3
Example

.ti 0
3. VMTP Server --> Client Protocol Events

.ti 0
XX. References

[1] Postel, J., "Simple Mail Transfer Protocol", STD 10, RFC 821,
.br
    USC/Information Sciences Institute, August, 1982.

[2] Kantor, B., and Lapsley, P., "Network News Transer Protocol",
.br
    RFC 977, U.C. San Diego/Berkely, February, 1986.

.ti 0
XX. Security Considerations

.ti 0
XX. Author's Address

.nf
Tad Hunt
Computer Science House
Rochester Institute of Technology
861 Grace Watson Hall
Rochester, NY 14623

Phone: (716) 475-3305

EMail: tad@csh.rit.edu
