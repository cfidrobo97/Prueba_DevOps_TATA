output "app_url" {
  value       = "http://${azurerm_container_group.main.fqdn}:${var.container_port}"
  description = "URL pública"
}
