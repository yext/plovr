// Generated by the protocol buffer compiler.  DO NOT EDIT!
// source: closure/closure-compiler/src/com/google/javascript/jscomp/instrumentation_template.proto

package com.google.javascript.jscomp;

public final class InstrumentationTemplate {
  private InstrumentationTemplate() {}
  public static void registerAllExtensions(
      com.google.protobuf.ExtensionRegistry registry) {
  }
  static com.google.protobuf.Descriptors.Descriptor
    internal_static_jscomp_Instrumentation_descriptor;
  static
    com.google.protobuf.GeneratedMessage.FieldAccessorTable
      internal_static_jscomp_Instrumentation_fieldAccessorTable;

  public static com.google.protobuf.Descriptors.FileDescriptor
      getDescriptor() {
    return descriptor;
  }
  private static com.google.protobuf.Descriptors.FileDescriptor
      descriptor;
  static {
    java.lang.String[] descriptorData = {
      "\nXclosure/closure-compiler/src/com/googl" +
      "e/javascript/jscomp/instrumentation_temp" +
      "late.proto\022\006jscomp\"\231\001\n\017Instrumentation\022\026" +
      "\n\016report_defined\030\001 \001(\t\022\023\n\013report_call\030\002 " +
      "\001(\t\022\023\n\013report_exit\030\006 \001(\t\022\035\n\025declaration_" +
      "to_remove\030\003 \003(\t\022\014\n\004init\030\004 \003(\t\022\027\n\017app_nam" +
      "e_setter\030\005 \001(\tB \n\034com.google.javascript." +
      "jscompP\001"
    };
    com.google.protobuf.Descriptors.FileDescriptor.InternalDescriptorAssigner assigner =
      new com.google.protobuf.Descriptors.FileDescriptor.InternalDescriptorAssigner() {
        public com.google.protobuf.ExtensionRegistry assignDescriptors(
            com.google.protobuf.Descriptors.FileDescriptor root) {
          descriptor = root;
          internal_static_jscomp_Instrumentation_descriptor =
            getDescriptor().getMessageTypes().get(0);
          internal_static_jscomp_Instrumentation_fieldAccessorTable = new
            com.google.protobuf.GeneratedMessage.FieldAccessorTable(
              internal_static_jscomp_Instrumentation_descriptor,
              new java.lang.String[] { "ReportDefined", "ReportCall", "ReportExit", "DeclarationToRemove", "Init", "AppNameSetter", });
          return null;
        }
      };
    com.google.protobuf.Descriptors.FileDescriptor
      .internalBuildGeneratedFileFrom(descriptorData,
        new com.google.protobuf.Descriptors.FileDescriptor[] {
        }, assigner);
  }

  // @@protoc_insertion_point(outer_class_scope)
}
