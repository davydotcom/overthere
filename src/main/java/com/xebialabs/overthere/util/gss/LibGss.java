package com.xebialabs.overthere.util.gss;

import com.sun.jna.Library;
import com.sun.jna.Native;
import com.sun.jna.Pointer;

/**
 * Created by davidestes on 4/14/16.
 */
public interface LibGss extends Library {
	LibGss INSTANCE = (LibGss) Native.loadLibrary("libgssapi_krb5.dylib", LibGss.class);
	//// Remember to free the allocated name (gss_name_t) space with gss_release_name
	int gss_import_name(Pointer minorStatus, Pointer inputName, GssOID oid, Pointer outputName);

	//int gss_export_name(int * minor_status, const gss_name_t input_name, gss_buffer_t exported_name);
	int gss_export_name(Pointer minorStatus, Pointer inputName, Pointer exportedName);

	//int gss_canonicalize_name(int * minor_status, const gss_name_t input_name, const gss_OID mech_type, gss_name_t * output_name)
	int gss_canonicalize_name(Pointer minorStatus, Pointer inputName, Pointer mechType, Pointer outputName);

	// int  gss_init_sec_context(int  *  minor_status, const gss_cred_id_t initiator_cred_handle,
	//   gss_ctx_id_t * context_handle, const gss_name_t target_name, const gss_OID mech_type, int req_flags,
	//   int time_req, const gss_channel_bindings_t input_chan_bindings, const gss_buffer_t input_token,
	//   gss_OID * actual_mech_type, gss_buffer_t output_token, int * ret_flags, int * time_rec);
	//attach_function :gss_init_sec_context, [Pointer, Pointer, Pointer, Pointer, Pointer, :int, :int, Pointer, Pointer, Pointer, Pointer, Pointer, Pointer], :int
	int gss_init_sec_context(Pointer minorStatus, Pointer initiatorCredHandle, Pointer contextHandle, Pointer targetName, GssOID mechType, int reqFlags, int timeReq, Pointer inputChanBindings, Pointer inputToken, Pointer actualMechType, Pointer outputToken, Pointer retFlags, Pointer timeRec);

	// int gss_accept_sec_context(int *minor_status, gss_ctx_id_t *context_handle, const gss_cred_id_t acceptor_cred_handle,
	//   const gss_buffer_t input_token_buffer, const gss_channel_bindings_t input_chan_bindings, gss_name_t *src_name, gss_OID *mech_type,
	//   gss_buffer_t output_token, int *ret_flags, int *time_rec, gss_cred_id_t *delegated_cred_handle);
	int gss_accept_sec_context(Pointer minorStatus, Pointer contextHandle, Pointer acceptorCredHandle, Pointer inputTokenBuffer, Pointer inputChanBindings, Pointer srcName, Pointer mechType, Pointer outputToken, Pointer retFlags, Pointer timeRec, Pointer delegatedCredHandle);

	// int gss_display_name(int * minor_status, gss_name_t input_name, gss_buffer_t output_name_buffer, gss_OID * output_name_type);
	int gss_display_name(Pointer minorStatus, Pointer inputName, Pointer outputNameBuffer, Pointer outputNameType);

	// int gss_acquire_cred(int *minor_status, const gss_name_t desired_name, int time_req, const gss_OID_set desired_mechs,
	//   gss_cred_usage_t cred_usage, gss_cred_id_t *output_cred_handle, gss_OID_set *actual_mechs, int *time_rec);
	//attach_function :gss_acquire_cred, [Pointer, Pointer, :int, Pointer, :int, Pointer, Pointer, Pointer], :int

	// int  gss_wrap(int  *  minor_status, const gss_ctx_id_t context_handle, int conf_req_flag,
	//   gss_qop_t qop_req, const gss_buffer_t input_message_buffer, int * conf_state, gss_buffer_t output_message_buffer);
	// @example:
	//   min_stat = FFI::MemoryPointer.new :int
	// Remember to free the allocated output_message_buffer with gss_release_buffer
	int gss_wrap(Pointer minorStatus, Pointer contextHandle, int confReqFlag, int qopReq, Pointer inputMessageBuffer, Pointer confState, Pointer outputMessageBuffer);

	// int  gss_unwrap(int  *  minor_status, const gss_ctx_id_t context_handle,
	//   const gss_buffer_t input_message_buffer, gss_buffer_t output_message_buffer, int * conf_state, gss_qop_t * qop_state);
	// @example:
	//   min_stat = FFI::MemoryPointer.new :int
	// Remember to free the allocated output_message_buffer with gss_release_buffer
	int gss_unwrap(Pointer minorStatus, Pointer contextHandle, Pointer inputMessageBuffer, Pointer outputMessageBuffer, Pointer confState, Pointer qopState);

	// int gss_get_mic(int * minor_status, const gss_ctx_id_t context_handle, gss_qop_t qop_req, const gss_buffer_t input_message_buffer, gss_buffer_t output_message_buffer)
	//attach_function :gss_get_mic, [Pointer, Pointer, :int, Pointer, Pointer], :int

	// int gss_delete_sec_context(int * minor_status, gss_ctx_id_t * context_handle, gss_buffer_t output_token);
	int gss_delete_sec_context(Pointer minorStatus, Pointer contextHandle, Pointer outputToken);

	// int gss_release_name(int * minor_status, gss_name_t * name);
	int gss_release_name(Pointer minorStatus, Pointer name);

	// int gss_release_buffer(int * minor_status, gss_buffer_t buffer);
	int gss_release_buffer(Pointer minorStatus, Pointer buffer);

	// int gss_release_cred(int *minor_status, gss_cred_id_t *cred_handle);
	int gss_release_cred(Pointer minorStatus, Pointer credHandle);

	// Used to register alternate keytabs
	// int krb5_gss_register_acceptor_identity(const char *);
	int krb5_gss_register_acceptor_identity(String identity);

	// int gss_display_status(int *minor_status, int status_value, int status_type, gss_OID mech_type, int *message_context, gss_buffer_t status_string)
	int gss_display_status(Pointer minorStatus, int statusValue, int statusType, Pointer mechType, Pointer messageContext, Pointer statusString);

	// int gss_krb5_copy_ccache(int *minor_status, gss_cred_id_t cred_handle, krb5_ccache out_ccache)
	int gss_krb5_copy_ccache(Pointer minorStatus, Pointer credHandle, Pointer outCCache);

	/** IOV Extensions **/

	// int GSSAPI_LIB_FUNCTION gss_wrap_iov( int * minor_status, gss_ctx_id_t  context_handle, int conf_req_flag, gss_qop_t  qop_req, int *  conf_state, gss_iov_buffer_desc *  iov, int  iov_count );
	//attach_function :gss_wrap_iov, [Pointer, Pointer, :int, :int, Pointer, Pointer, :int], :int
	int gss_wrap_iov(Pointer minorStatus, Pointer contextHandle, int conf_req_flag, int qop_req, Pointer conf_state, GssIOVBufferDesc[] iov, int iovCount);



	// int GSSAPI_LIB_FUNCTION gss_unwrap_iov ( int * minor_status, gss_ctx_id_t context_handle,
													 //int * conf_state, gss_qop_t * qop_state, gss_iov_buffer_desc * iov, int iov_count )

	int gss_unwrap_iov(Pointer minorStatus, Pointer contextHandle, Pointer confState, Pointer qopState, GssIOVBufferDesc[] iov, int iovCount);

	// int GSSAPI_LIB_CALL gss_wrap_iov_length ( int * minor_status, gss_ctx_id_t context_handle, int conf_req_flag, gss_qop_t  qop_req, int *  conf_state, gss_iov_buffer_desc * iov, int iov_count)
	//attach_function :gss_wrap_iov_length, [Pointer, Pointer, :int, :int, Pointer, Pointer, :int], :int
	int gss_wrap_iov_length(Pointer minorStatus, Pointer contextHandle, int conf_req_flag, int qop_req, Pointer conf_state, GssIOVBufferDesc[] iov, int iovCount);


	/*
	 * Variable Definitions
	*/

	//Flag bits for context-level services.

	int GSS_C_DELEG_FLAG        = 1;
	int GSS_C_MUTUAL_FLAG       = 2;
	int GSS_C_REPLAY_FLAG       = 4;
	int GSS_C_SEQUENCE_FLAG     = 8;
	int GSS_C_CONF_FLAG         = 16;
	int GSS_C_INTEG_FLAG        = 32;
	int GSS_C_ANON_FLAG         = 64;
	int GSS_C_PROT_READY_FLAG   = 128;
	int GSS_C_TRANS_FLAG        = 256;
	int GSS_C_DELEG_POLICY_FLAG = 32768;

	//Credential Usage Options
	int GSS_C_BOTH = 0;
	int GSS_C_INITIATE = 1;
	int GSS_C_ACCEPT = 2;

	//Misc Constants
	int GSS_C_INDEFINITE = 0xffffffff; //Expiration time of 2^32-1 seconds means infinite lifetime for sec or cred context


	// Message Offsets
	int GSS_C_CALLING_ERROR_OFFSET = 24;
	int GSS_C_ROUTINE_ERROR_OFFSET = 16;
	int GSS_C_SUPPLEMENTARY_OFFSET = 0;

	//QOP (Quality of Protection)
	int GSS_C_QOP_DEFAULT = 0;

	//GSSAPI Status & Error Codes
	int GSS_S_COMPLETE  = 0;
	int GSS_C_GSS_CODE  = 1;
	int GSS_C_MECH_CODE = 2;
//
//	GSS_C_CALLING_ERRORS = {
//		(1 << GSS_C_CALLING_ERROR_OFFSET) => "GSS_S_CALL_INACCESSIBLE_READ",
//				(2 << GSS_C_CALLING_ERROR_OFFSET) => "GSS_S_CALL_INACCESSIBLE_WRITE",
//				(3 << GSS_C_CALLING_ERROR_OFFSET) => "GSS_S_CALL_BAD_STRUCTURE"
//	}
//
//	GSS_C_SUPPLEMENTARY_CODES = {
//		(1 << (GSS_C_SUPPLEMENTARY_OFFSET + 0)) => "GSS_S_CONTINUE_NEEDED",
//				(1 << (GSS_C_SUPPLEMENTARY_OFFSET + 1)) => "GSS_S_DUPLICATE_TOKEN",
//				(1 << (GSS_C_SUPPLEMENTARY_OFFSET + 2)) => "GSS_S_OLD_TOKEN",
//				(1 << (GSS_C_SUPPLEMENTARY_OFFSET + 3)) => "GSS_S_UNSEQ_TOKEN",
//				(1 << (GSS_C_SUPPLEMENTARY_OFFSET + 4)) => "GSS_S_GAP_TOKEN"
//	}
//
//	GSS_C_ROUTINE_ERRORS = {
//		(1 << GSS_C_ROUTINE_ERROR_OFFSET) => "GSS_S_BAD_MECH",
//				(2 << GSS_C_ROUTINE_ERROR_OFFSET) => "GSS_S_BAD_NAME",
//				(3 << GSS_C_ROUTINE_ERROR_OFFSET) => "GSS_S_BAD_NAMETYPE",
//				(4 << GSS_C_ROUTINE_ERROR_OFFSET) => "GSS_S_BAD_BINDINGS",
//				(5 << GSS_C_ROUTINE_ERROR_OFFSET) => "GSS_S_BAD_STATUS",
//				(6 << GSS_C_ROUTINE_ERROR_OFFSET) => "GSS_S_BAD_SIG",
//				(7 << GSS_C_ROUTINE_ERROR_OFFSET) => "GSS_S_NO_CRED",
//				(8 << GSS_C_ROUTINE_ERROR_OFFSET) => "GSS_S_NO_CONTEXT",
//				(9 << GSS_C_ROUTINE_ERROR_OFFSET) => "GSS_S_DEFECTIVE_TOKEN",
//				(10 << GSS_C_ROUTINE_ERROR_OFFSET) => "GSS_S_DEFECTIVE_CREDENTIAL",
//				(11 << GSS_C_ROUTINE_ERROR_OFFSET) => "GSS_S_CREDENTIALS_EXPIRED",
//				(12 << GSS_C_ROUTINE_ERROR_OFFSET) => "GSS_S_CONTEXT_EXPIRED",
//				(13 << GSS_C_ROUTINE_ERROR_OFFSET) => "GSS_S_FAILURE",
//				(14 << GSS_C_ROUTINE_ERROR_OFFSET) => "GSS_S_BAD_QOP",
//				(15 << GSS_C_ROUTINE_ERROR_OFFSET) => "GSS_S_UNAUTHORIZED",
//				(16 << GSS_C_ROUTINE_ERROR_OFFSET) => "GSS_S_UNAVAILABLE",
//				(17 << GSS_C_ROUTINE_ERROR_OFFSET) => "GSS_S_DUPLICATE_ELEMENT",
//				(18 << GSS_C_ROUTINE_ERROR_OFFSET) => "GSS_S_NAME_NOT_MN"
//	}
//


	// IOV Buffer Types (gssapi_ext.h)
	int GSS_IOV_BUFFER_TYPE_EMPTY       = 0;
	int GSS_IOV_BUFFER_TYPE_DATA        = 1;  // Packet data
	int GSS_IOV_BUFFER_TYPE_HEADER      = 2;  // Mechanism header
	int GSS_IOV_BUFFER_TYPE_MECH_PARAMS = 3;  // Mechanism specific parameters
	int GSS_IOV_BUFFER_TYPE_TRAILER     = 7;  // Mechanism trailer
	int GSS_IOV_BUFFER_TYPE_PADDING     = 9;  // Padding
	int GSS_IOV_BUFFER_TYPE_STREAM      = 10; // Complete wrap token
	int GSS_IOV_BUFFER_TYPE_SIGN_ONLY   = 11; // Sign only packet data

	// Flags
	int GSS_IOV_BUFFER_FLAG_MASK        = 0xFFFF0000;
	int GSS_IOV_BUFFER_FLAG_ALLOCATE    = 0x00010000; // indicates GSS should allocate
	int GSS_IOV_BUFFER_FLAG_ALLOCATED   = 0x00020000; // indicates caller should free

	// Various Null values. (gssapi.h)
	Pointer GSS_C_NO_NAME               = Pointer.NULL; // ((gss_name_t) 0);
	Pointer GSS_C_NO_BUFFER             = Pointer.NULL; // ((gss_buffer_t) 0);
	GssOID GSS_C_NO_OID                 = null; // ((gss_OID) 0);
	Pointer GSS_C_NO_OID_SET            = Pointer.NULL; // ((gss_OID_set) 0);
	Pointer GSS_C_NO_CONTEXT            = Pointer.NULL; // ((gss_ctx_id_t) 0);
	Pointer GSS_C_NO_CREDENTIAL         = Pointer.NULL; // ((gss_cred_id_t) 0);
	Pointer GSS_C_NO_CHANNEL_BINDINGS   = Pointer.NULL; // ((gss_channel_bindings_t) 0);
	GssBufferDesc GSS_C_EMPTY_BUFFER    = new GssBufferDesc();

}
